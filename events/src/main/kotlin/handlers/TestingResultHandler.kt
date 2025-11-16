package handlers

import dtos.responses.TestingResultEvent
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class TestingResultHandler(
    private val messagingTemplate: SimpMessagingTemplate,
) : TestingResultHandlerInt {

    private val log = LoggerFactory.getLogger(TestingResultHandler::class.java)

    override fun handleTestingResult(result: TestingResultEvent) {
        try {
            log.info(
                "📋 Resultado de Test Recibido: testId=${result.testId}, snippetId=${result.snippetId}, requestId=${result.requestId}",
            )

            if (result.passed) {
                log.info(
                    "✅ TEST APROBADO: testId=${result.testId}, snippetId=${result.snippetId}",
                )
            } else {
                log.warn(
                    "❌ TEST FAILED - testId=${result.testId}, snippetId=${result.snippetId}",
                )
                log.debug("Expected outputs: {}", result.expectedOutputs)
                log.debug("Actual outputs: {}", result.outputs)
                if (result.errors.isNotEmpty()) {
                    log.warn("Test errors: ${result.errors.joinToString(", ")}")
                }
            }

            val topic = "/topic/snippet/${result.snippetId}/test-results"
            messagingTemplate.convertAndSend(topic, result)
            log.info("Resultado de test enviado a WebSocket topic: $topic")
        } catch (e: Exception) {
            val stackTrace = e.stackTrace.firstOrNull()
            val location =
                stackTrace?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"
            log.error(
                "Error procesando testing result para test ${result.testId} en $location: ${e.message}",
                e,
            )
        }
    }
}
