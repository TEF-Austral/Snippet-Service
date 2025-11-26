package dtos.responses

data class ValidationResponseDTO(
    val isValid: Boolean,
    val violations: List<LintViolationDTO>,
)
