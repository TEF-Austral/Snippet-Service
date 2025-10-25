package services

import clients.AssetServiceClient
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
) : SnippetService {

    override fun createSnippet(requestDTO: SnippetRequestDTO): SnippetResponseDTO {
        val entity =
            Snippet(
                name = requestDTO.name,
                description = requestDTO.description,
                bucketKey = "default-bucket-key",
                bucketContainer = "snippets",
                language = requestDTO.language,
                version = requestDTO.version,
            )
        val saved = repository.save(entity)
        return saved.toDto()
    }

    override fun getSnippetById(id: Long): SnippetResponseDTO {
        val entity =
            repository.findById(id).orElseThrow { NoSuchElementException("Snippet not found: $id") }
        return entity.toDto()
    }

    override fun getSnippetsByBucketId(bucketId: String): List<SnippetResponseDTO> =
        repository.findByBucketId(bucketId).map {
            it.toDto()
        }

    override fun getOwnerSnippets(ownerId: String): List<SnippetResponseDTO> =
        repository.findByOwnerId(ownerId).map {
            it.toDto()
        }

    @Transactional
    override fun updateSnippet(
        id: Long,
        requestDTO: UpdateSnippetDTO,
    ): SnippetResponseDTO {
        val existing =
            repository.findById(id).orElseThrow { NoSuchElementException("Snippet not found: $id") }

        requestDTO.content?.let { content ->
            assetServiceClient.createOrUpdateAsset(
                container = existing.bucketContainer,
                key =
                    existing.bucketKey ?: throw IllegalStateException(
                        "Snippet has no bucket key",
                    ),
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
    override fun deleteSnippet(id: Long) {
        val existing =
            repository.findById(id).orElseThrow { NoSuchElementException("Snippet not found: $id") }

        assetServiceClient.deleteAsset(
            container = existing.bucketContainer,
            key = existing.bucketKey ?: throw IllegalStateException("Snippet has no bucket key"),
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
