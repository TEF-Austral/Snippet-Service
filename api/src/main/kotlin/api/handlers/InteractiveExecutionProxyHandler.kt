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
import entity.Snippet

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
        try {
            establishConnection(downstreamSession)
        } catch (e: Exception) {
            handleError(downstreamSession, e)
        }
    }

    private fun establishConnection(downstreamSession: WebSocketSession) {
        val (snippetId, token) = extractAttributes(downstreamSession)

        logConnectionEstablished(snippetId, downstreamSession.id)

        val userId = decodeUserId(token, jwtDecoder)

        logJwtDecoded(userId, snippetId)

        val snippet = findSnippet(snippetId, snippetRepository)

        if (!checkAndHandlePermission(userId, snippetId, snippet, downstreamSession)) return

        val m2mToken = getAndHandleM2MToken(snippetId, userId, downstreamSession) ?: return

        val upstreamSession = connectUpstream(printScriptServiceDomain, m2mToken, downstreamSession)

        logUpstreamConnected(snippetId, upstreamSession.id)

        sendInitMessage(upstreamSession, snippet, objectMapper)

        logExecutionInitialized(snippetId, userId, snippet)

        downstreamSession.attributes["UPSTREAM_SESSION"] = upstreamSession
    }

    override fun handleTextMessage(
        downstreamSession: WebSocketSession,
        message: TextMessage,
    ) {
        try {
            val upstreamSession = getUpstreamSession(downstreamSession)
            if (upstreamSession?.isOpen == true) {
                upstreamSession.sendMessage(message)
                logMessageForwarded(downstreamSession.id, message.payloadLength)
            } else {
                logUpstreamNotAvailable(downstreamSession.id)
            }
        } catch (e: Exception) {
            logTextMessageError(downstreamSession.id, e)
        }
    }

    private fun logConnectionEstablished(
        snippetId: Long,
        sessionId: String,
    ) {
        log.info(
            "WebSocket connection established for interactive execution: " +
                "snippetId=$snippetId, sessionId=$sessionId",
        )
    }

    private fun logJwtDecoded(
        userId: String,
        snippetId: Long,
    ) {
        log.debug("JWT decoded successfully: userId=$userId, snippetId=$snippetId")
    }

    private fun checkAndHandlePermission(
        userId: String,
        snippetId: Long,
        snippet: Snippet,
        session: WebSocketSession,
    ): Boolean {
        if (!checkPermission(userId, snippetId, snippet, authorizationServiceClient)) {
            log.warn(
                "User does not have permission for interactive execution: userId=$userId, snippetId=$snippetId",
            )
            session.close(
                CloseStatus.POLICY_VIOLATION.withReason(
                    "You don't have permission to execute this snippet",
                ),
            )
            return false
        }
        log.debug("Permission check passed: userId=$userId, snippetId=$snippetId")
        return true
    }

    private fun getAndHandleM2MToken(
        snippetId: Long,
        userId: String,
        session: WebSocketSession,
    ): String? {
        val m2mToken = getM2MToken()
        if (m2mToken == null) {
            log.error(
                "Failed to obtain M2M token for interactive execution: snippetId=$snippetId, userId=$userId",
            )
            session.close(
                CloseStatus.SERVER_ERROR.withReason("Failed to obtain M2M token"),
            )
            return null
        }
        log.debug("M2M token obtained successfully: snippetId=$snippetId")
        return m2mToken
    }

    private fun logUpstreamConnected(
        snippetId: Long,
        upstreamSessionId: String,
    ) {
        log.debug(
            "Upstream WebSocket connected: snippetId=$snippetId, upstreamSessionId=$upstreamSessionId",
        )
    }

    private fun logExecutionInitialized(
        snippetId: Long,
        userId: String,
        snippet: Snippet,
    ) {
        log.info(
            "Interactive execution initialized successfully: snippetId=$snippetId, userId=$userId, container=${snippet.bucketContainer}, key=${snippet.bucketKey}",
        )
    }

    private fun handleError(
        downstreamSession: WebSocketSession,
        e: Exception,
    ) {
        logError(e)
        sendErrorMessage(downstreamSession, e)
        closeSessionOnError(downstreamSession, e)
    }

    private fun logError(e: Exception) {
        val stackTrace = e.stackTrace.firstOrNull()
        val location =
            stackTrace?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"
        log.error(
            "Error establishing WebSocket proxy connection at $location: error=${e.message}",
            e,
        )
    }

    private fun sendErrorMessage(
        session: WebSocketSession,
        e: Exception,
    ) {
        try {
            session.sendMessage(
                TextMessage("{\"type\":\"Error\", \"value\":\"Internal error: ${e.message}\"}"),
            )
        } catch (closeException: Exception) {
            log.error(
                "Error sending error message: ${closeException.message}",
                closeException,
            )
        }
    }

    private fun closeSessionOnError(
        session: WebSocketSession,
        e: Exception,
    ) {
        try {
            session.close(
                CloseStatus.SERVER_ERROR.withReason(e.message ?: "Internal error"),
            )
        } catch (closeException: Exception) {
            log.error(
                "Error closing WebSocket session after error: ${closeException.message}",
                closeException,
            )
        }
    }

    private fun getUpstreamSession(downstreamSession: WebSocketSession): WebSocketSession? =
        downstreamSession.attributes["UPSTREAM_SESSION"] as? WebSocketSession

    private fun logMessageForwarded(
        sessionId: String,
        messageSize: Int,
    ) {
        log.debug(
            "Message forwarded to upstream: sessionId=$sessionId, messageSize=$messageSize",
        )
    }

    private fun logUpstreamNotAvailable(sessionId: String) {
        log.warn(
            "Upstream session not available or closed: sessionId=$sessionId",
        )
    }

    private fun logTextMessageError(
        sessionId: String,
        e: Exception,
    ) {
        val stackTrace = e.stackTrace.firstOrNull()
        val location =
            stackTrace?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"
        log.error(
            "Error handling text message at $location: sessionId=$sessionId, error=${e.message}",
            e,
        )
    }

    override fun afterConnectionClosed(
        downstreamSession: WebSocketSession,
        status: CloseStatus,
    ) {
        try {
            val upstreamSession = getUpstreamSession(downstreamSession)
            if (upstreamSession?.isOpen == true) {
                upstreamSession.close(status)
                logUpstreamClosed(downstreamSession.id, status)
            }
            logConnectionClosed(downstreamSession.id, status)
        } catch (e: Exception) {
            logConnectionClosedError(downstreamSession.id, e)
        }
    }

    private fun logUpstreamClosed(
        sessionId: String,
        status: CloseStatus,
    ) {
        log.info(
            "Upstream WebSocket closed: sessionId=$sessionId, status=$status",
        )
    }

    private fun logConnectionClosed(
        sessionId: String,
        status: CloseStatus,
    ) {
        log.info(
            "WebSocket connection closed: sessionId=$sessionId, status=$status",
        )
    }

    private fun logConnectionClosedError(
        sessionId: String,
        e: Exception,
    ) {
        val stackTrace = e.stackTrace.firstOrNull()
        val location =
            stackTrace?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"
        log.error(
            "Error closing WebSocket connection at $location: sessionId=$sessionId, error=${e.message}",
            e,
        )
    }

    private fun getM2MToken(): String? =
        try {
            log.debug("Requesting M2M token")
            val token = requestM2MToken()
            logTokenResult(token)
            token
        } catch (e: Exception) {
            logM2MTokenError(e)
            null
        }

    private fun requestM2MToken(): String? {
        val authorizeRequest =
            OAuth2AuthorizeRequest
                .withClientRegistrationId("auth0-m2m")
                .principal("SnippetServiceM2M")
                .build()
        val authorizedClient = m2mClientManager.authorize(authorizeRequest)
        return authorizedClient?.accessToken?.tokenValue
    }

    private fun logTokenResult(token: String?) {
        if (token != null) {
            log.debug("M2M token obtained successfully")
        } else {
            log.warn("M2M token is null")
        }
    }

    private fun logM2MTokenError(e: Exception) {
        val stackTrace = e.stackTrace.firstOrNull()
        val location =
            stackTrace?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"
        log.error("Error getting M2M token at $location: ${e.message}", e)
    }

    private fun extractAttributes(downstreamSession: WebSocketSession): Pair<Long, String> {
        val snippetId = downstreamSession.attributes["snippetId"] as Long
        val token = downstreamSession.attributes["token"].toString()
        return Pair(snippetId, token)
    }

    private fun decodeUserId(
        token: String,
        jwtDecoder: JwtDecoder,
    ): String {
        val jwt = jwtDecoder.decode(token)
        return jwt.subject
    }

    private fun findSnippet(
        snippetId: Long,
        snippetRepository: SnippetRepository,
    ): Snippet =
        snippetRepository.findById(snippetId).orElseThrow {
            NoSuchElementException("Snippet not found: $snippetId")
        }

    private fun checkPermission(
        userId: String,
        snippetId: Long,
        snippet: Snippet,
        authorizationServiceClient: AuthorizationService,
    ): Boolean =
        authorizationServiceClient.checkPermission(
            userId = userId,
            action = UserAction.EDIT,
            snippetId = snippetId.toString(),
            ownerId = snippet.ownerId,
        )

    private fun connectUpstream(
        printScriptServiceDomain: String,
        m2mToken: String,
        downstreamSession: WebSocketSession,
    ): WebSocketSession {
        val upstreamHandler = UpstreamHandler(downstreamSession)
        val upstreamUrl = "ws://$printScriptServiceDomain/ws/execute-interactive?token=$m2mToken"
        return webSocketClient.execute(upstreamHandler, upstreamUrl).get()
    }

    private fun sendInitMessage(
        upstreamSession: WebSocketSession,
        snippet: Snippet,
        objectMapper: com.fasterxml.jackson.databind.ObjectMapper,
    ) {
        val initMessage =
            mapOf(
                "type" to "InitExecution",
                "bucketContainer" to snippet.bucketContainer,
                "bucketKey" to snippet.bucketKey,
                "version" to snippet.version,
            )
        upstreamSession.sendMessage(TextMessage(objectMapper.writeValueAsString(initMessage)))
    }
}
