package snippet.dtos

import java.time.LocalDateTime

data class SnippetDTO(
    val id: Long,
    val name: String,
    val code: String,
    val deletedAt: LocalDateTime? = null,
)

data class CreateSnippetRequest(
    val name: String,
    val code: String,
)

data class UpdateSnippetRequest(
    val name: String,
    val code: String,
)
