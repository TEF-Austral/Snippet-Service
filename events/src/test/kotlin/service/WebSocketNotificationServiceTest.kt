package service

import dtos.responses.TestingResultEvent
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.messaging.simp.SimpMessagingTemplate

@ExtendWith(MockitoExtension::class)
class WebSocketNotificationServiceTest {

    @Mock
    lateinit var template: SimpMessagingTemplate

    @Test
    fun `sends message to topic`() {
        val service = WebSocketNotificationService(template)
        val evt = TestingResultEvent("r", 1L, 2L, true, listOf("a"), listOf("a"), emptyList())

        service.sendTestResult(evt)

        @Suppress("UNCHECKED_CAST")
        verify(
            template,
        ).convertAndSend(
            org.mockito.ArgumentMatchers.eq("/topic/snippet/2/test-results"),
            org.mockito.ArgumentMatchers.any(Map::class.java),
        )
    }
}
