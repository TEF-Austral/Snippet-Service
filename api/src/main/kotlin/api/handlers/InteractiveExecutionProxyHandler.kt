package api.handlers

import authorization.AuthorizationService
import authorization.UserAction
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.handler.TextWebSocketHandler
import repositories.SnippetRepository
import java.util.NoSuchElementException

@Component
class InteractiveExecutionProxyHandler(
    private val snippetRepository: SnippetRepository,
    private val authorizationServiceClient: AuthorizationService,
    private val jwtDecoder: JwtDecoder,
    private val m2mClientManager: OAuth2AuthorizedClientManager,
    @param:Value($$"${printscript.service.domain}") private val printScriptServiceDomain: String,
) : TextWebSocketHandler() {
    private val log = LoggerFactory.getLogger(InteractiveExecutionProxyHandler::class.java)
    private val webSocketClient = StandardWebSocketClient()
    private val objectMapper = jacksonObjectMapper()

    override fun afterConnectionEstablished(downstreamSession: WebSocketSession) {
        var snippetId: Long? = null
        var userId: String? = null

        try {
            snippetId = downstreamSession.attributes["snippetId"] as Long
            val token = downstreamSession.attributes["token"].toString()

            log.info(
                "WebSocket connection established for interactive execution: snippetId=$snippetId, sessionId=${downstreamSession.id}",
            )

            val jwt = jwtDecoder.decode(token)
            userId = jwt.subject

            log.debug("JWT decoded successfully: userId=$userId, snippetId=$snippetId")

            val snippet =
                snippetRepository
                    .findById(snippetId)
                    .orElseThrow { NoSuchElementException("Snippet not found: $snippetId") }

            val hasPermission =
                authorizationServiceClient.checkPermission(
                    userId = userId,
                    action = UserAction.EDIT,
                    snippetId = snippetId.toString(),
                    ownerId = snippet.ownerId,
                )

            if (!hasPermission) {
                log.warn(
                    "User does not have permission for interactive execution: userId=$userId, snippetId=$snippetId",
                )
                downstreamSession.close(
                    CloseStatus.POLICY_VIOLATION.withReason(
                        "You don't have permission to execute this snippet",
                    ),
                )
                return
            }

            log.debug("Permission check passed: userId=$userId, snippetId=$snippetId")

            val m2mToken = getM2MToken()
            if (m2mToken == null) {
                log.error(
                    "Failed to obtain M2M token for interactive execution: snippetId=$snippetId, userId=$userId",
                )
                downstreamSession.close(
                    CloseStatus.SERVER_ERROR.withReason("Failed to obtain M2M token"),
                )
                return
            }

            log.debug("M2M token obtained successfully: snippetId=$snippetId")

            val upstreamHandler = UpstreamHandler(downstreamSession)

            val upstreamUrl =
                "wss://$printScriptServiceDomain/ws/execute-interactive?token=$m2mToken"

            log.debug("Connecting to upstream WebSocket: snippetId=$snippetId, url=$upstreamUrl")

            val upstreamSession = webSocketClient.execute(upstreamHandler, upstreamUrl).get()

            log.debug(
                "Upstream WebSocket connected: snippetId=$snippetId, upstreamSessionId=${upstreamSession.id}",
            )

            val initMessage =
                mapOf(
                    "type" to "InitExecution",
                    "bucketContainer" to snippet.bucketContainer,
                    "bucketKey" to snippet.bucketKey,
                    "version" to snippet.version,
                )
            upstreamSession.sendMessage(TextMessage(objectMapper.writeValueAsString(initMessage)))

            log.info(
                "Interactive execution initialized successfully: snippetId=$snippetId, userId=$userId, container=${snippet.bucketContainer}, key=${snippet.bucketKey}",
            )

            downstreamSession.attributes["UPSTREAM_SESSION"] = upstreamSession
        } catch (e: Exception) {
            val stackTrace = e.stackTrace.firstOrNull()
            val location =
                stackTrace?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"
            log.error(
                "Error establishing WebSocket proxy connection at $location: snippetId=$snippetId, userId=$userId, error=${e.message}",
                e,
            )

            try {
                downstreamSession.sendMessage(
                    TextMessage("{\"type\":\"Error\", \"value\":\"Internal error: ${e.message}\"}"),
                )
                downstreamSession.close(
                    CloseStatus.SERVER_ERROR.withReason(e.message ?: "Internal error"),
                )
            } catch (closeException: Exception) {
                log.error(
                    "Error closing WebSocket session after error: ${closeException.message}",
                    closeException,
                )
            }
        }
    }

    override fun handleTextMessage(
        downstreamSession: WebSocketSession,
        message: TextMessage,
    ) {
        try {
            val upstreamSession =
                downstreamSession.attributes["UPSTREAM_SESSION"]
                    as? WebSocketSession
            if (upstreamSession?.isOpen == true) {
                upstreamSession.sendMessage(message)
                log.debug(
                    "Message forwarded to upstream: sessionId=${downstreamSession.id}, messageSize=${message.payloadLength}",
                )
            } else {
                log.warn(
                    "Upstream session not available or closed: sessionId=${downstreamSession.id}",
                )
            }
        } catch (e: Exception) {
            val stackTrace = e.stackTrace.firstOrNull()
            val location =
                stackTrace?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"
            log.error(
                "Error handling text message at $location: sessionId=${downstreamSession.id}, error=${e.message}",
                e,
            )
        }
    }

    override fun afterConnectionClosed(
        downstreamSession: WebSocketSession,
        status: CloseStatus,
    ) {
        try {
            val upstreamSession =
                downstreamSession.attributes["UPSTREAM_SESSION"]
                    as? WebSocketSession
            if (upstreamSession?.isOpen == true) {
                upstreamSession.close(status)
                log.info(
                    "Upstream WebSocket closed: sessionId=${downstreamSession.id}, status=$status",
                )
            }
            log.info(
                "WebSocket connection closed: sessionId=${downstreamSession.id}, status=$status",
            )
        } catch (e: Exception) {
            val stackTrace = e.stackTrace.firstOrNull()
            val location =
                stackTrace?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"
            log.error(
                "Error closing WebSocket connection at $location: sessionId=${downstreamSession.id}, error=${e.message}",
                e,
            )
        }
    }

    private fun getM2MToken(): String? =
        try {
            log.debug("Requesting M2M token")
            val authorizeRequest =
                OAuth2AuthorizeRequest
                    .withClientRegistrationId("auth0-m2m")
                    .principal("SnippetServiceM2M")
                    .build()

            val authorizedClient = m2mClientManager.authorize(authorizeRequest)
            val token = authorizedClient?.accessToken?.tokenValue

            if (token != null) {
                log.debug("M2M token obtained successfully")
            } else {
                log.warn("M2M token is null")
            }

            token
        } catch (e: Exception) {
            val stackTrace = e.stackTrace.firstOrNull()
            val location =
                stackTrace?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"
            log.error("Error getting M2M token at $location: ${e.message}", e)
            null
        }
}
