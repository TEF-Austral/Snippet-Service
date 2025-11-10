package snippet.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import snippet.handlers.InteractiveExecutionProxyHandler

@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val proxyHandler: InteractiveExecutionProxyHandler,
    private val authInterceptor: AuthHandshakeInterceptor,
    @Value("\${app.websocket.allowed-origins}") private val allowedOrigins: String,
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry
            .addHandler(proxyHandler, "/ws/execute-interactive")
            .addInterceptors(authInterceptor)
            .setAllowedOrigins(allowedOrigins)
    }
}
