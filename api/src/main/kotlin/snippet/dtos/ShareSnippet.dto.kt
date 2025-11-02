package snippet.dtos

data class ShareSnippetDTO(
    val userId: String,
    val canRead: Boolean = true,
    val canEdit: Boolean = false,
)
