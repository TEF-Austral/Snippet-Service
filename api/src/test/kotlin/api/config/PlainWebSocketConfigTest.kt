package api.config

import api.handlers.InteractiveExecutionProxyHandler
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@ExtendWith(MockitoExtension::class)
class PlainWebSocketConfigTest {

    @Mock
    private lateinit var proxyHandler: InteractiveExecutionProxyHandler

    @Mock
    private lateinit var authInterceptor: AuthHandshakeInterceptor

    @Mock
    private lateinit var registry: WebSocketHandlerRegistry

    @Mock
    private lateinit var handlerRegistration: WebSocketHandlerRegistration

    @Test
    fun `registerWebSocketHandlers configures handler with interceptor and allowed origins`() {
        val allowedOrigins = "http://localhost:3000"
        val config = PlainWebSocketConfig(proxyHandler, authInterceptor, allowedOrigins)

        `when`(registry.addHandler(proxyHandler, "/api/ws/execute-interactive"))
            .thenReturn(handlerRegistration)
        `when`(handlerRegistration.addInterceptors(authInterceptor))
            .thenReturn(handlerRegistration)
        `when`(handlerRegistration.setAllowedOrigins(allowedOrigins))
            .thenReturn(handlerRegistration)

        config.registerWebSocketHandlers(registry)

        verify(registry).addHandler(proxyHandler, "/api/ws/execute-interactive")
        verify(handlerRegistration).addInterceptors(authInterceptor)
        verify(handlerRegistration).setAllowedOrigins(allowedOrigins)
    }
}
