package service

import dtos.responses.TestingResultEvent
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class WebSocketNotificationService(
    private val messagingTemplate: SimpMessagingTemplate,
) {
    private val log = LoggerFactory.getLogger(WebSocketNotificationService::class.java)

    fun sendTestResult(result: TestingResultEvent) {
        try {
            val topic = "/topic/snippet/${result.snippetId}/test-results"

            val payload =
                mapOf(
                    "testId" to result.testId,
                    "passed" to result.passed,
                    "outputs" to result.outputs,
                    "expectedOutputs" to result.expectedOutputs,
                    "errors" to result.errors,
                )

            messagingTemplate.convertAndSend(topic, payload)
            log.debug("Sent test result notification to topic: $topic")
        } catch (e: Exception) {
            log.error("Error sending test result notification: ${e.message}", e)
        }
    }
}
