package handlers

import dtos.responses.FormattingResultEventDTO

interface FormattingResultHandlerInt {

    fun handleFormattingResult(result: FormattingResultEventDTO)
}
