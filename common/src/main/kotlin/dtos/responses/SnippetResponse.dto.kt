package dtos.responses

import dtos.types.Language

data class SnippetResponseDTO(
    val snippetId: Long?,
    val name: String,
    val description: String,
    val content: String,
    val language: Language,
    val version: String?,
    val author: String,
    val complianceStatus: String,
    val validationErrors: String?,
)
