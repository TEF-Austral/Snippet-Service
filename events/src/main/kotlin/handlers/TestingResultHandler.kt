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
                "Processing testing result: testId=${result.testId}, snippetId=${result.snippetId}, requestId=${result.requestId}",
            )
// Hola fondo norte
            if (result.passed) {
                log.info(
                    "Test passed: testId=${result.testId}, snippetId=${result.snippetId}, requestId=${result.requestId}",
                )
            } else {
                log.warn(
                    "Test failed: testId=${result.testId}, snippetId=${result.snippetId}, requestId=${result.requestId}",
                )
                log.debug("Expected outputs: {}", result.expectedOutputs)
                log.debug("Actual outputs: {}", result.outputs)
                if (result.errors.isNotEmpty()) {
                    log.warn("Test errors: ${result.errors.joinToString(", ")}")
                }
            }
        } catch (e: Exception) {
            val stackTrace = e.stackTrace.firstOrNull()
            val location =
                stackTrace?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"
            log.error(
                "Unexpected error processing testing result for test ${result.testId} at $location: ${e.message}",
                e,
            )
        }
    }
}
