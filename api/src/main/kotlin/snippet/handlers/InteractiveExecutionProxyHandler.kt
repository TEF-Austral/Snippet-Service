package snippet.handlers

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.handler.TextWebSocketHandler
import snippet.component.AuthorizationServiceClient
import snippet.repositories.SnippetRepository
import snippet.security.AuthenticatedUserProvider
import java.util.NoSuchElementException

@Component
class InteractiveExecutionProxyHandler(
    private val snippetRepository: SnippetRepository,
    private val authorizationServiceClient: AuthorizationServiceClient,
    private val authenticatedUserProvider: AuthenticatedUserProvider,
    @Value("\${printscript.service.url}") private val printScriptServiceUrl: String,
) : TextWebSocketHandler() {

    private val webSocketClient = StandardWebSocketClient()

    override fun afterConnectionEstablished(downstreamSession: WebSocketSession) {
        try {
            val snippetId =
                downstreamSession.attributes["snippetId"] as? Long
                    ?: throw IllegalArgumentException("No snippetId en la sesión de WebSocket")

            val userId = authenticatedUserProvider.getCurrentUserId()

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

            val upstreamUrl =
                "ws://$printScriptServiceUrl/ws/execute-interactive?snippetId=$snippetId"

            val upstreamSession = webSocketClient.execute(upstreamHandler, upstreamUrl).get()

            downstreamSession.attributes["UPSTREAM_SESSION"] = upstreamSession
        } catch (e: Exception) {
            println("Error al establecer la conexión proxy de WebSocket: ${e.message}")
            downstreamSession.sendMessage(
                TextMessage("{\"type\":\"error\", \"value\":\"Error interno: ${e.message}\"}"),
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
