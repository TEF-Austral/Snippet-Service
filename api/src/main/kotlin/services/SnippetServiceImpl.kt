package services

import component.AssetServiceClient
import security.AuthorizationServiceClient
import dtos.SnippetRequestDTO
import dtos.UpdateSnippetDTO
import dtos.SnippetResponseDTO
import entities.Snippet
import repositories.SnippetRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SnippetServiceImpl(
    private val repository: SnippetRepository,
    private val assetServiceClient: AssetServiceClient,
    private val authorizationServiceClient: AuthorizationServiceClient,
) : SnippetService {

    override fun createSnippet(
        requestDTO: SnippetRequestDTO,
        ownerId: String,
    ): SnippetResponseDTO {
        val entity =
            Snippet(
                name = requestDTO.name,
                description = requestDTO.description,
                ownerId = ownerId,
                bucketContainer = "snippets",
                language = requestDTO.language,
                version = requestDTO.version,
            )
        val saved = repository.save(entity)
        return saved.toDto()
    }

    override fun getSnippetById(
        id: Long,
        requesterId: String,
    ): SnippetResponseDTO {
        val entity =
            repository
                .findById(id)
                .orElseThrow { NoSuchElementException("Snippet not found: $id") }

        val hasPermission =
            authorizationServiceClient.checkPermission(
                userId = requesterId,
                snippetId = id.toString(),
                action = "read",
                ownerId = entity.ownerId,
            )

        if (!hasPermission) {
            throw IllegalAccessException("You don't have permission to access this snippet")
        }

        return entity.toDto()
    }

    override fun getOwnerSnippets(ownerId: String): List<SnippetResponseDTO> {
        val ownedSnippets = repository.findByOwnerId(ownerId)

        val sharedSnippetIds = authorizationServiceClient.getUserSnippetIds(ownerId)

        val sharedSnippets =
            sharedSnippetIds
                .mapNotNull { snippetId ->
                    try {
                        snippetId.toLongOrNull()?.let { repository.findById(it).orElse(null) }
                    } catch (e: Exception) {
                        null
                    }
                }

        val allSnippets = (ownedSnippets + sharedSnippets).distinctBy { it.id }

        return allSnippets.map { it.toDto() }
    }

    @Transactional
    override fun updateSnippet(
        id: Long,
        requestDTO: UpdateSnippetDTO,
        requesterId: String,
    ): SnippetResponseDTO {
        val existing =
            repository
                .findById(id)
                .orElseThrow { NoSuchElementException("Snippet not found: $id") }

        val hasPermission =
            authorizationServiceClient.checkPermission(
                userId = requesterId,
                snippetId = id.toString(),
                action = "edit",
                ownerId = existing.ownerId,
            )

        if (!hasPermission) {
            throw IllegalAccessException("You don't have permission to update this snippet")
        }

        requestDTO.content?.let { content ->
            assetServiceClient.createOrUpdateAsset(
                container = existing.bucketContainer,
                key =
                    existing.bucketKey
                        ?: throw IllegalStateException("Snippet has no bucket key"),
                content = content,
            )
        }

        existing.name = requestDTO.name ?: existing.name
        existing.description = requestDTO.description ?: existing.description
        existing.language = requestDTO.language ?: existing.language
        existing.version = requestDTO.version ?: existing.version

        val saved = repository.save(existing)
        return saved.toDto()
    }

    @Transactional
    override fun deleteSnippet(
        id: Long,
        requesterId: String,
    ) {
        val existing =
            repository
                .findById(id)
                .orElseThrow { NoSuchElementException("Snippet not found: $id") }

        val hasPermission =
            authorizationServiceClient.checkPermission(
                userId = requesterId,
                snippetId = id.toString(),
                action = "delete",
                ownerId = existing.ownerId,
            )

        if (!hasPermission) {
            throw IllegalAccessException("You don't have permission to delete this snippet")
        }

        assetServiceClient.deleteAsset(
            container = existing.bucketContainer,
            key =
                existing.bucketKey
                    ?: throw IllegalStateException("Snippet has no bucket key"),
        )

        repository.deleteById(id)
    }

    private fun Snippet.toDto() =
        SnippetResponseDTO(
            snippetId = this.id ?: 0L,
            name = this.name,
            description = this.description,
            bucketContainer = this.bucketContainer,
            bucketKey = this.bucketKey ?: "",
            language = this.language,
            version = this.version,
        )
}
