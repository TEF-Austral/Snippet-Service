package snippet.handlers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.handler.TextWebSocketHandler
import snippet.component.AuthorizationServiceClient
import snippet.repositories.SnippetRepository
import java.util.NoSuchElementException

@Component
class InteractiveExecutionProxyHandler(
    private val snippetRepository: SnippetRepository,
    private val authorizationServiceClient: AuthorizationServiceClient,
    private val jwtDecoder: JwtDecoder,
    @Value("\${printscript.service.domain}") private val printScriptServiceDomain: String,
) : TextWebSocketHandler() {

    private val webSocketClient = StandardWebSocketClient()
    private val objectMapper = jacksonObjectMapper()

    override fun afterConnectionEstablished(downstreamSession: WebSocketSession) {
        try {
            val snippetId = downstreamSession.attributes["snippetId"] as Long
            val token = downstreamSession.attributes["token"] as String

            val jwt = jwtDecoder.decode(token)
            val userId = jwt.subject

            val snippet =
                snippetRepository
                    .findById(snippetId)
                    .orElseThrow { NoSuchElementException("Snippet no encontrado: $snippetId") }

            val hasPermission =
                authorizationServiceClient.checkPermission(
                    userId = userId,
                    action = "edit",
                    snippetId = snippetId.toString(),
                    ownerId = snippet.ownerId,
                )

            if (!hasPermission) {
                downstreamSession.close(
                    CloseStatus.POLICY_VIOLATION.withReason(
                        "No tienes permisos para ejecutar este snippet",
                    ),
                )
                return
            }

            val upstreamHandler = UpstreamHandler(downstreamSession)
            val upstreamUrl = "ws://$printScriptServiceDomain/ws/execute-interactive"
            val upstreamSession = webSocketClient.execute(upstreamHandler, upstreamUrl).get()

            val initMessage =
                mapOf(
                    "type" to "InitExecution",
                    "bucketContainer" to snippet.bucketContainer,
                    "bucketKey" to snippet.bucketKey,
                    "version" to snippet.version,
                )
            upstreamSession.sendMessage(TextMessage(objectMapper.writeValueAsString(initMessage)))

            downstreamSession.attributes["UPSTREAM_SESSION"] = upstreamSession
        } catch (e: Exception) {
            println("Error al establecer la conexión proxy de WebSocket: ${e.message}")
            downstreamSession.sendMessage(
                TextMessage("{\"type\":\"Error\", \"value\":\"Error interno: ${e.message}\"}"),
            )
            downstreamSession.close(
                CloseStatus.SERVER_ERROR.withReason(e.message ?: "Error interno"),
            )
        }
    }

    override fun handleTextMessage(
        downstreamSession: WebSocketSession,
        message: TextMessage,
    ) {
        val upstreamSession = downstreamSession.attributes["UPSTREAM_SESSION"] as? WebSocketSession
        if (upstreamSession?.isOpen == true) {
            upstreamSession.sendMessage(message)
        }
    }

    override fun afterConnectionClosed(
        downstreamSession: WebSocketSession,
        status: CloseStatus,
    ) {
        val upstreamSession = downstreamSession.attributes["UPSTREAM_SESSION"] as? WebSocketSession
        if (upstreamSession?.isOpen == true) {
            upstreamSession.close(status)
        }
    }
}
