package dtos.responses

import common.dtos.responses.LintViolationDTO

data class ValidationResponseDTO(
    val isValid: Boolean,
    val violations: List<LintViolationDTO>,
)
