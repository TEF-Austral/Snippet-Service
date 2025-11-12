package dtos.requests

import dtos.types.Language

data class UpdateSnippetRequestDTO(
    val name: String?,
    val content: String?,
    val description: String?,
    var language: Language?,
    val version: String?,
)
