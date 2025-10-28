package dtos

import entities.Language

data class SnippetResponseDTO(
    val snippetId: Long?,
    val name: String,
    val description: String,
    val bucketKey: String?,
    val bucketContainer: String,
    val language: Language,
    val version: String?,
)
