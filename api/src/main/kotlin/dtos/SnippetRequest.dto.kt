package dtos

import Language
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class SnippetRequestDTO(
    @field:NotBlank(message = "Name is required")
    val name: String,
    @field:NotBlank(message = "Description is required")
    val description: String,
    @field:NotNull(message = "Language is required")
    var language: Language,
    @field:NotBlank(message = "Version is required")
    val version: String,
)
