package api.config

import api.handlers.InteractiveExecutionProxyHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class PlainWebSocketConfig(
    private val proxyHandler: InteractiveExecutionProxyHandler,
    private val authInterceptor: AuthHandshakeInterceptor,
    @param:Value("\${app.websocket.allowed-origins}") private val allowedOrigins: String,
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry
            .addHandler(proxyHandler, "/api/ws/execute-interactive")
            .addInterceptors(authInterceptor)
            .setAllowedOrigins(allowedOrigins)
    }
}
