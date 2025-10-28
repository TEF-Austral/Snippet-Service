package dtos

import entities.Language

data class UpdateSnippetDTO(
    val name: String?,
    val content: String?,
    val description: String?,
    var language: Language?,
    val version: String?,
)
