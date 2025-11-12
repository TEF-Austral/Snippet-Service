package common.dtos.requests

data class ValidateContentRequestDTO(
    val content: String,
    val language: String,
    val version: String,
)
