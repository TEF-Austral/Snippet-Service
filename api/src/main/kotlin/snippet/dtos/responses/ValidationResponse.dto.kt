package snippet.dtos.responses

import snippet.dtos.LintViolationDTO

data class ValidationResponseDTO(
    val isValid: Boolean,
    val violations: List<LintViolationDTO>,
)
