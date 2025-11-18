package dtos.responses

data class LintViolationDTO(
    val message: String,
    val line: Int,
    val column: Int,
)
