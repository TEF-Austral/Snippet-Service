package snippet.services

import org.springframework.stereotype.Service
import results.TestingResultEvent

@Service
class TestingResultHandler {
    private val log = org.slf4j.LoggerFactory.getLogger(TestingResultHandler::class.java)

    fun handleTestingResult(result: TestingResultEvent) {
        log.info("Processing testing result for test ${result.testId}, passed: ${result.passed}")
        try {
            println("🔔 [Snippet Service] Processing testing result for test ${result.testId}")

            if (result.passed) {
                log.warn("Test ${result.testId} passed for snippet ${result.snippetId}")
                println(
                    "✅ [Snippet Service] Test ${result.testId} passed for snippet ${result.snippetId}",
                )
            } else {
                log.warn("Test ${result.testId} failed for snippet ${result.snippetId}")
                println(
                    "❌ [Snippet Service] Test ${result.testId} failed for snippet ${result.snippetId}",
                )
                println("   Expected: ${result.expectedOutputs}")
                println("   Got: ${result.outputs}")
                println("   Errors: ${result.errors.joinToString(", ")}")
            }
        } catch (e: Exception) {
            log.warn(
                "Unexpected error processing testing result for test ${result.testId}: ${e.message}",
            )
            println(
                "❌ [Snippet Service] Unexpected error processing testing result for test ${result.testId}: ${e.message}",
            )
            e.printStackTrace()
        }
    }
}
