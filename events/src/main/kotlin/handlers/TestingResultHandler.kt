package handlers

import org.springframework.stereotype.Service
import dtos.responses.TestingResultEvent

@Service
class TestingResultHandler : TestingResultHandlerInt {

    override fun handleTestingResult(result: TestingResultEvent) {
        try {
            println("[Snippet Service] Processing testing result for test ${result.testId}")

            if (result.passed) {
                println(
                    "[Snippet Service] Test ${result.testId} passed for snippet ${result.snippetId}",
                )
            } else {
                println(
                    "[Snippet Service] Test ${result.testId} failed for snippet ${result.snippetId}",
                )
                println("   Expected: ${result.expectedOutputs}")
                println("   Got: ${result.outputs}")
                println("   Errors: ${result.errors.joinToString(", ")}")
            }
        } catch (e: Exception) {
            println(
                "[Snippet Service] Unexpected error processing testing result for test ${result.testId}: ${e.message}",
            )
            e.printStackTrace()
        }
    }
}
