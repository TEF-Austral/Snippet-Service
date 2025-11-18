package api.handlers

import authorization.AuthorizationService
import authorization.UserAction
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import entity.Snippet
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

            val snippet = getSnippet(snippetId)

            if (hasExecutionPermission(userId, snippetId, snippet, downstreamSession)) return

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

            val upstreamHandler = UpstreamHandler(downstreamSession)

            val upstreamSession =
                setupExecutionSession(m2mToken, upstreamHandler, snippet, snippetId, userId)

            downstreamSession.attributes["UPSTREAM_SESSION"] = upstreamSession
        } catch (e: Exception) {
            handleException(e, snippetId, userId, downstreamSession)
        }
    }

    private fun handleException(
        e: Exception,
        snippetId: Long?,
        userId: String?,
        downstreamSession: WebSocketSession,
    ) {
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

    private fun setupExecutionSession(
        m2mToken: String,
        upstreamHandler: UpstreamHandler,
        snippet: Snippet,
        snippetId: Long,
        userId: String?,
    ): WebSocketSession? {
        val upstreamUrl =
            "ws://$printScriptServiceDomain/ws/execute-interactive?token=$m2mToken"

        val upstreamSession = webSocketClient.execute(upstreamHandler, upstreamUrl).get()

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
        return upstreamSession
    }

    private fun getSnippet(snippetId: Long): Snippet =
        snippetRepository
            .findById(snippetId)
            .orElseThrow { NoSuchElementException("Snippet not found: $snippetId") }

    private fun hasExecutionPermission(
        userId: String,
        snippetId: Long,
        snippet: Snippet?,
        downstreamSession: WebSocketSession,
    ): Boolean {
        val hasPermission =
            authorizationServiceClient.checkPermission(
                userId = userId,
                action = UserAction.EDIT,
                snippetId = snippetId.toString(),
                ownerId = snippet!!.ownerId,
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
            return true
        }
        return false
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

    private fun getM2MToken(): String? {
        try {
            val authorizeRequest =
                OAuth2AuthorizeRequest
                    .withClientRegistrationId("auth0-m2m")
                    .principal("SnippetServiceM2M")
                    .build()

            val authorizedClient = m2mClientManager.authorize(authorizeRequest)
            val token = authorizedClient?.accessToken?.tokenValue
            return token
        } catch (e: Exception) {
            return handleM2MTokenError(e)
        }
    }

    private fun handleM2MTokenError(e: Exception): Nothing? {
        val stackTrace = e.stackTrace.firstOrNull()
        val location =
            stackTrace?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"
        log.error("Error getting M2M token at $location: ${e.message}", e)
        return null
    }
}
