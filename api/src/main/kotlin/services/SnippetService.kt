package services

import dtos.SnippetRequestDTO
import dtos.SnippetResponseDTO
import dtos.UpdateSnippetDTO

interface SnippetService {
    fun createSnippet(requestDTO: SnippetRequestDTO): SnippetResponseDTO

    fun getSnippetById(id: Long): SnippetResponseDTO?

    fun getSnippetsByBucketId(bucketId: String): List<SnippetResponseDTO>

    fun updateSnippet(
        id: Long,
        requestDTO: UpdateSnippetDTO,
    ): SnippetResponseDTO

    fun deleteSnippet(id: Long)

    fun getOwnerSnippets(ownerId: String): List<SnippetResponseDTO>

    fun getSnippetThatUserHasAccess(userId: String): List<SnippetResponseDTO>
}
