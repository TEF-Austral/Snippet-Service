package snippet.dtos

data class ShareSnippetResponseDTO(
    val message: String,
    val snippetId: Long,
    val sharedWith: String,
    val permissions: PermissionsSummary,
)

data class PermissionsSummary(
    val canRead: Boolean,
    val canEdit: Boolean,
)
