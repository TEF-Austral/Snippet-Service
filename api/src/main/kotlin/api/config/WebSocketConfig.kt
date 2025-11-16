package api.config

import api.handlers.InteractiveExecutionProxyHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.springframework.web.socket.config.annotation.StompEndpointRegistry

@Configuration
@EnableWebSocket
@EnableWebSocketMessageBroker
class WebSocketConfig(
    private val proxyHandler: InteractiveExecutionProxyHandler,
    private val authInterceptor: AuthHandshakeInterceptor,
    @param:Value("\${app.websocket.allowed-origins}") private val allowedOrigins: String,
) : WebSocketConfigurer,
    WebSocketMessageBrokerConfigurer {

    // 3. ESTE ES TU CÓDIGO ACTUAL PARA WEBSOCKETS "CRUDOS"
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry
            .addHandler(proxyHandler, "/ws/execute-interactive")
            .addInterceptors(authInterceptor)
            .setAllowedOrigins(allowedOrigins)
    }

    // 4. ESTO ES LO NUEVO PARA HABILITAR STOMP (Y EL SimpMessagingTemplate)
    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker("/topic")
        registry.setApplicationDestinationPrefixes("/app")
    }

    // 5. ESTO REGISTRA EL ENDPOINT DE CONEXIÓN PARA STOMP
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry
            .addEndpoint("/ws")
            .addInterceptors(authInterceptor)
            .setAllowedOrigins(allowedOrigins)
            .withSockJS()
    }
}
