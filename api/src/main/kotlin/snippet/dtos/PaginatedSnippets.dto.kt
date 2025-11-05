package snippet.dtos

import snippet.dtos.responses.SnippetResponseDTO

data class PaginatedSnippetsDTO(
    val page: Int,
    val pageSize: Int,
    val count: Long,
    val snippets: List<SnippetResponseDTO>,
)
