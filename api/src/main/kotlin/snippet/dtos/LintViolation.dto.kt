package snippet.dtos

data class LintViolationDTO(
    val message: String,
    val line: Int,
    val column: Int,
)
