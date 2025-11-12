package common.dtos.responses

data class PaginatedSnippetsDTO(
    val page: Int,
    val pageSize: Int,
    val count: Long,
    val snippets: List<SnippetResponseDTO>,
)
