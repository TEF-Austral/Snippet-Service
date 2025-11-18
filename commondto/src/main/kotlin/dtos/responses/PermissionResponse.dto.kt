package dtos.responses

data class PermissionResponseDTO(
    val id: Long?,
    val userId: String,
    val snippetId: String,
    val canRead: Boolean,
    val canEdit: Boolean,
)
