package api.handlers

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.Mockito.never
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession

@ExtendWith(MockitoExtension::class)
class UpstreamHandlerTest {

    @Test
    fun `closes downstream when connection closed`() {
        val downstream = Mockito.mock(WebSocketSession::class.java)
        val session = Mockito.mock(WebSocketSession::class.java)
        val handler = UpstreamHandler(downstream)
        val status = CloseStatus.NORMAL

        Mockito.`when`(downstream.isOpen).thenReturn(true)

        handler.afterConnectionClosed(session, status)

        verify(downstream).close(status)
    }

    @Test
    fun `does not close downstream when already closed`() {
        val downstream = Mockito.mock(WebSocketSession::class.java)
        val session = Mockito.mock(WebSocketSession::class.java)
        val handler = UpstreamHandler(downstream)
        val status = CloseStatus.NORMAL

        Mockito.`when`(downstream.isOpen).thenReturn(false)

        handler.afterConnectionClosed(session, status)

        verify(downstream, never()).close(status)
    }

    @Test
    fun `forwards message to downstream when open via handleMessage`() {
        val downstream = Mockito.mock(WebSocketSession::class.java)
        val session = Mockito.mock(WebSocketSession::class.java)
        val handler = UpstreamHandler(downstream)
        val message = TextMessage("test message")

        Mockito.`when`(downstream.isOpen).thenReturn(true)

        handler.handleMessage(session, message)

        verify(downstream).sendMessage(message)
    }

    @Test
    fun `does not forward message when downstream is closed via handleMessage`() {
        val downstream = Mockito.mock(WebSocketSession::class.java)
        val session = Mockito.mock(WebSocketSession::class.java)
        val handler = UpstreamHandler(downstream)
        val message = TextMessage("test message")

        Mockito.`when`(downstream.isOpen).thenReturn(false)

        handler.handleMessage(session, message)

        verify(downstream, never()).sendMessage(message)
    }
}
