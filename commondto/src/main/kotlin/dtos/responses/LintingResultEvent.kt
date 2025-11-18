package dtos.responses

data class LintingResultEvent(
    val requestId: String,
    val snippetId: Long,
    val isValid: Boolean,
    val violations: List<ViolationDTO> = emptyList(),
)

data class ViolationDTO(
    val message: String,
    val line: Int,
    val column: Int,
)
