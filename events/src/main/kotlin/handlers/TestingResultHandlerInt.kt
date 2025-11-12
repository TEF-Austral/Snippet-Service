package handlers

import dtos.responses.TestingResultEvent

interface TestingResultHandlerInt {
    fun handleTestingResult(result: TestingResultEvent)
}
