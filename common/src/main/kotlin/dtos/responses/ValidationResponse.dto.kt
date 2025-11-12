package dtos.responses

import dtos.responses.LintViolationDTO

data class ValidationResponseDTO(
    val isValid: Boolean,
    val violations: List<LintViolationDTO>,
)
