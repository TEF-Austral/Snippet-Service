package api.config

import api.handlers.InteractiveExecutionProxyHandler
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@ExtendWith(MockitoExtension::class)
class WebSocketConfigTest {

    @Mock
    private lateinit var proxyHandler: InteractiveExecutionProxyHandler

    @Mock
    private lateinit var authInterceptor: AuthHandshakeInterceptor

    @Mock
    private lateinit var messageBrokerRegistry: MessageBrokerRegistry

    @Mock
    private lateinit var stompEndpointRegistry: StompEndpointRegistry

    @Mock
    private lateinit var stompWebSocketEndpointRegistration: StompWebSocketEndpointRegistration

    @Mock
    private lateinit var webSocketHandlerRegistry: WebSocketHandlerRegistry

    @Mock
    private lateinit var handlerRegistration: WebSocketHandlerRegistration

    @Test
    fun `configureMessageBroker enables simple broker and sets application prefix`() {
        val config = WebSocketConfig(proxyHandler, authInterceptor, "http://localhost:3000")

        config.configureMessageBroker(messageBrokerRegistry)

        verify(messageBrokerRegistry).enableSimpleBroker("/topic")
        verify(messageBrokerRegistry).setApplicationDestinationPrefixes("/app")
    }

    @Test
    fun `registerStompEndpoints registers endpoint with allowed origins and SockJS`() {
        val config = WebSocketConfig(proxyHandler, authInterceptor, "http://localhost:3000")

        `when`(stompEndpointRegistry.addEndpoint("/ws"))
            .thenReturn(stompWebSocketEndpointRegistration)
        `when`(stompWebSocketEndpointRegistration.setAllowedOrigins("http://localhost:3000"))
            .thenReturn(stompWebSocketEndpointRegistration)

        config.registerStompEndpoints(stompEndpointRegistry)

        verify(stompEndpointRegistry).addEndpoint("/ws")
        verify(stompWebSocketEndpointRegistration).setAllowedOrigins("http://localhost:3000")
        verify(stompWebSocketEndpointRegistration).withSockJS()
    }

    @Test
    fun `registerWebSocketHandlers configures handler with interceptor and allowed origins`() {
        val config = WebSocketConfig(proxyHandler, authInterceptor, "http://localhost:3000")

        `when`(webSocketHandlerRegistry.addHandler(proxyHandler, "/api/ws/execute-interactive"))
            .thenReturn(handlerRegistration)
        `when`(handlerRegistration.addInterceptors(authInterceptor))
            .thenReturn(handlerRegistration)
        `when`(handlerRegistration.setAllowedOrigins("http://localhost:3000"))
            .thenReturn(handlerRegistration)

        config.registerWebSocketHandlers(webSocketHandlerRegistry)

        verify(webSocketHandlerRegistry).addHandler(proxyHandler, "/api/ws/execute-interactive")
        verify(handlerRegistration).addInterceptors(authInterceptor)
        verify(handlerRegistration).setAllowedOrigins("http://localhost:3000")
    }
}
