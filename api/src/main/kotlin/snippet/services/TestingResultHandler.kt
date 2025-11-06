package snippet.services

import org.springframework.stereotype.Service
import results.TestingResultEvent

@Service
class TestingResultHandler {

    fun handleTestingResult(result: TestingResultEvent) {
        println("🔔 [Snippet Service] Processing testing result for test ${result.testId}")

        if (result.passed) {
            println(
                "✅ [Snippet Service] Test ${result.testId} passed for snippet ${result.snippetId}",
            )
        } else {
            println(
                "❌ [Snippet Service] Test ${result.testId} failed for snippet ${result.snippetId}",
            )
            println("   Expected: ${result.expectedOutputs}")
            println("   Got: ${result.outputs}")
            println("   Errors: ${result.errors.joinToString(", ")}")
        }
    }
}
