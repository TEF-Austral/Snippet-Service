package api.config

import api.handlers.InteractiveExecutionProxyHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(
    private val proxyHandler: InteractiveExecutionProxyHandler,
    private val authInterceptor: AuthHandshakeInterceptor,
    @param:Value("\${app.websocket.allowed-origins}") private val allowedOrigins: String,
) : WebSocketMessageBrokerConfigurer,
    WebSocketConfigurer {

    // Configure STOMP message broker for test results
    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker("/topic") // Enable in-memory broker for /topic
        registry.setApplicationDestinationPrefixes("/app") // Application destination prefix
    }

    // Register STOMP endpoint with SockJS
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry
            .addEndpoint("/ws")
            .setAllowedOrigins(allowedOrigins)
            .withSockJS()
    }

    // Register custom WebSocket handler for interactive execution
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry
            .addHandler(proxyHandler, "/ws/execute-interactive")
            .addInterceptors(authInterceptor)
            .setAllowedOrigins(allowedOrigins)
    }
}
