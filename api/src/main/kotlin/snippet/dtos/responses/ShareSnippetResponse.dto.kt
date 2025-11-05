package snippet.dtos.responses

import snippet.dtos.PermissionsSummaryDTO

data class ShareSnippetResponseDTO(
    val message: String,
    val snippetId: Long,
    val sharedWith: String,
    val permissions: PermissionsSummaryDTO,
)
