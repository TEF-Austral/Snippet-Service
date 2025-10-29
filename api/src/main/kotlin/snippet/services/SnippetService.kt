package snippet.services

import snippet.dtos.SnippetRequestDTO
import snippet.dtos.SnippetResponseDTO
import snippet.dtos.UpdateSnippetDTO

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
