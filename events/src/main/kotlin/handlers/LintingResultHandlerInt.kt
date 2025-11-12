package handlers

import dtos.responses.LintingResultEvent

interface LintingResultHandlerInt {

    fun handleLintingResult(result: LintingResultEvent)
}
