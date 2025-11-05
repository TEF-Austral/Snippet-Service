package snippet.services

import snippet.dtos.requests.CreateSnippetRequestDTO
import snippet.dtos.PaginatedSnippetsDTO
import snippet.dtos.SnippetFilterDTO
import snippet.dtos.responses.SnippetResponseDTO
import snippet.dtos.requests.UpdateSnippetRequestDTO

interface SnippetService {
    fun createSnippet(
        requestDTO: CreateSnippetRequestDTO,
        ownerId: String,
        author: String,
    ): SnippetResponseDTO

    fun getSnippetById(
        id: Long,
        requesterId: String,
    ): SnippetResponseDTO

    fun updateSnippet(
        id: Long,
        requestDTO: UpdateSnippetRequestDTO,
        requesterId: String,
    ): SnippetResponseDTO

    fun deleteSnippet(
        id: Long,
        requesterId: String,
    )

    fun getSnippetsThatUserHaveAcces(
        requesterId: String,
        page: Int,
        pageSize: Int,
    ): PaginatedSnippetsDTO

    fun getSnippetThatUserIsOwner(
        requesterId: String,
        page: Int,
        pageSize: Int,
    ): PaginatedSnippetsDTO

    fun getAllMySnippets(
        requesterId: String,
        page: Int,
        pageSize: Int,
    ): PaginatedSnippetsDTO

    fun getMySnippets(
        requesterId: String,
        page: Int,
        pageSize: Int,
        filterDTO: SnippetFilterDTO,
    ): PaginatedSnippetsDTO
}
