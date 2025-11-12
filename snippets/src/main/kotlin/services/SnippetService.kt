package services

import common.dtos.requests.CreateSnippetRequestDTO
import common.dtos.requests.SnippetFilterDTO
import common.dtos.requests.UpdateSnippetRequestDTO
import common.dtos.responses.PaginatedSnippetsDTO
import common.dtos.responses.SnippetResponseDTO
import common.dtos.responses.StreamSnippetResponseDTO

interface SnippetService {
    fun createSnippet(
        requestDTO: CreateSnippetRequestDTO,
        ownerId: String,
        author: String,
        bucketContainer: String = "snippets",
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

    fun getSnippetsThatUserHaveAccess(
        requesterId: String,
        page: Int,
        pageSize: Int,
    ): PaginatedSnippetsDTO

    fun getSnippetsThatUserHavePermission(requesterId: String): List<StreamSnippetResponseDTO>

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
