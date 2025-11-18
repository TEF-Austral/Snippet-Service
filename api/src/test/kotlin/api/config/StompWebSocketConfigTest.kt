package api.config

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration

@ExtendWith(MockitoExtension::class)
class StompWebSocketConfigTest {

    @Mock
    private lateinit var messageBrokerRegistry: MessageBrokerRegistry

    @Mock
    private lateinit var stompEndpointRegistry: StompEndpointRegistry

    @Mock
    private lateinit var stompWebSocketEndpointRegistration: StompWebSocketEndpointRegistration

    @Test
    fun `configureMessageBroker enables simple broker and sets application prefix`() {
        val config = StompWebSocketConfig("http://localhost:3000")

        config.configureMessageBroker(messageBrokerRegistry)

        verify(messageBrokerRegistry).enableSimpleBroker("/topic")
        verify(messageBrokerRegistry).setApplicationDestinationPrefixes("/app")
    }

    @Test
    fun `registerStompEndpoints registers endpoint with allowed origins and SockJS`() {
        val config = StompWebSocketConfig("http://localhost:3000,http://localhost:4200")

        org.mockito.Mockito
            .`when`(stompEndpointRegistry.addEndpoint("/ws"))
            .thenReturn(stompWebSocketEndpointRegistration)
        org.mockito.Mockito
            .`when`(
                stompWebSocketEndpointRegistration.setAllowedOrigins(
                    "http://localhost:3000,http://localhost:4200",
                ),
            ).thenReturn(stompWebSocketEndpointRegistration)

        config.registerStompEndpoints(stompEndpointRegistry)

        verify(stompEndpointRegistry).addEndpoint("/ws")
        verify(
            stompWebSocketEndpointRegistration,
        ).setAllowedOrigins("http://localhost:3000,http://localhost:4200")
        verify(stompWebSocketEndpointRegistration).withSockJS()
    }
}
