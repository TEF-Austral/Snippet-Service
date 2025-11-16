// events/src/main/kotlin/handlers/TestingResultHandler.kt

package handlers

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import dtos.responses.TestingResultEvent

@Service
class TestingResultHandler : TestingResultHandlerInt {
    private val log = LoggerFactory.getLogger(TestingResultHandler::class.java)

    override fun handleTestingResult(result: TestingResultEvent) {
        try {
            log.info(
                "📋 Test Result Received - testId=${result.testId}, snippetId=${result.snippetId}, requestId=${result.requestId}",
            )

            if (result.passed) {
                log.info(
                    "✅ TEST PASSED - testId=${result.testId}, snippetId=${result.snippetId}",
                )
                log.debug("Expected: ${result.expectedOutputs}")
                log.debug("Actual: ${result.outputs}")
            } else {
                log.warn(
                    "❌ TEST FAILED - testId=${result.testId}, snippetId=${result.snippetId}",
                )
                log.warn("Expected outputs: ${result.expectedOutputs}")
                log.warn("Actual outputs: ${result.outputs}")

                if (result.errors.isNotEmpty()) {
                    log.error("Errors: ${result.errors.joinToString(", ")}")
                }
            }

            // 🆕 TODO: Aquí puedes agregar lógica adicional si necesitas:
            // - Guardar estadísticas de tests
            // - Notificar al usuario vía WebSocket
            // - Actualizar métricas de calidad del snippet
            // - Enviar emails/notificaciones
        } catch (e: Exception) {
            val stackTrace = e.stackTrace.firstOrNull()
            val location =
                stackTrace?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"
            log.error(
                "Error processing testing result for test ${result.testId} at $location: ${e.message}",
                e,
            )
        }
    }
}
