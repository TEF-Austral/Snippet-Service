package common.dtos.requests

data class ShareSnippetDTO(
    val userId: String,
    val canRead: Boolean = true,
    val canEdit: Boolean = false,
)
