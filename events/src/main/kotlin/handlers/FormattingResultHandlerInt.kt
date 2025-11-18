package handlers

import dtos.responses.FormattingResultEvent

interface FormattingResultHandlerInt {

    fun handleFormattingResult(result: FormattingResultEvent)
}
