package dtos

import entities.Language

data class SnippetResponseDTO(
    val snippetId: Long,
    val name: String,
    val description: String,
    val bucketId: String,
    val language: Language,
    val version: String?,
)
