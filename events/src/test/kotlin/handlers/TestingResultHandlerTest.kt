package handlers

import dtos.responses.TestingResultEvent
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import service.WebSocketNotificationService

@ExtendWith(MockitoExtension::class)
class TestingResultHandlerTest {

    @Mock
    lateinit var ws: WebSocketNotificationService

    @Test
    fun `delegates to websocket service`() {
        val handler = TestingResultHandler(ws)
        val evt =
            TestingResultEvent(
                requestId = "r",
                testId = 1L,
                snippetId = 2L,
                passed = true,
                outputs = listOf("a"),
                expectedOutputs = listOf("a"),
                errors = emptyList(),
            )

        handler.handleTestingResult(evt)

        verify(ws).sendTestResult(evt)
    }
}
