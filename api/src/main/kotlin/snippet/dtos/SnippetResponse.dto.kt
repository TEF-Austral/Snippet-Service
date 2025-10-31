package snippet.dtos

import common.Language

data class SnippetResponseDTO(
    val snippetId: Long?,
    val name: String,
    val description: String,
    val content: String,
    val language: Language,
    val version: String?,
    val author: String,
)
