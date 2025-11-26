package dtos.responses

data class FormattingResultEventDTO(
    val requestId: String,
    val snippetId: Long,
    val success: Boolean,
    val formattedContent: String? = null,
    val error: String? = null,
)
