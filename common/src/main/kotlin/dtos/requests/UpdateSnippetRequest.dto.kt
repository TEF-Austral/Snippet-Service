package common.dtos.requests
import common.dtos.types.Language

data class UpdateSnippetRequestDTO(
    val name: String?,
    val content: String?,
    val description: String?,
    var language: Language?,
    val version: String?,
)
