package snippet.services

import snippet.dtos.CreateSnippetDTO
import snippet.dtos.PaginatedSnippetsDTO
import snippet.dtos.SnippetResponseDTO
import snippet.dtos.UpdateSnippetDTO

interface SnippetService {
    fun createSnippet(
        requestDTO: CreateSnippetDTO,
        ownerId: String,
        author: String,
    ): SnippetResponseDTO

    fun getSnippetById(
        id: Long,
        requesterId: String,
    ): SnippetResponseDTO

    fun updateSnippet(
        id: Long,
        requestDTO: UpdateSnippetDTO,
        requesterId: String,
    ): SnippetResponseDTO

    fun deleteSnippet(
        id: Long,
        requesterId: String,
    )

    fun getMySnippets(
        requesterId: String,
        page: Int,
        pageSize: Int,
    ): PaginatedSnippetsDTO
}
