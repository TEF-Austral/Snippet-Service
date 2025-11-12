package api.handlers

import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

class UpstreamHandler(
    private val downstreamSession: WebSocketSession,
) : TextWebSocketHandler() {

    override fun handleTextMessage(
        session: WebSocketSession,
        message: TextMessage,
    ) {
        if (downstreamSession.isOpen) {
            downstreamSession.sendMessage(message)
        }
    }

    override fun afterConnectionClosed(
        session: WebSocketSession,
        status: CloseStatus,
    ) {
        if (downstreamSession.isOpen) {
            downstreamSession.close(status)
        }
    }
}
