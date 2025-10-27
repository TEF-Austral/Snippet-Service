package services

import dtos.SnippetRequestDTO
import dtos.SnippetResponseDTO
import dtos.UpdateSnippetDTO

interface SnippetService {
    fun createSnippet(
        requestDTO: SnippetRequestDTO,
        ownerId: String,
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

    fun getOwnerSnippets(ownerId: String): List<SnippetResponseDTO>
}
