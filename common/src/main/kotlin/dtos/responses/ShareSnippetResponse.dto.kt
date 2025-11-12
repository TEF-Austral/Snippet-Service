package common.dtos.responses

data class ShareSnippetResponseDTO(
    val message: String,
    val snippetId: Long,
    val sharedWith: String,
    val permissions: PermissionsSummaryDTO,
)
