package snippet.services

import events.SnippetEventProducer
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import snippet.component.AssetServiceClient
import snippet.dtos.CreateSnippetDTO
import snippet.dtos.PaginatedSnippetsDTO
import snippet.dtos.SnippetResponseDTO
import snippet.dtos.UpdateSnippetDTO
import snippet.entities.Snippet
import snippet.repositories.SnippetRepository

@Service
class SnippetServiceImpl(
    private val repository: SnippetRepository,
    private val assetServiceClient: AssetServiceClient,
    private val eventProducer: SnippetEventProducer,
) : SnippetService {

    @Transactional
    override fun createSnippet(
        requestDTO: CreateSnippetDTO,
        ownerId: String,
        author: String,
    ): SnippetResponseDTO {
        val entity =
            Snippet(
                name = requestDTO.name,
                description = requestDTO.description,
                ownerId = ownerId,
                bucketContainer = "snippets",
                language = requestDTO.language,
                version = requestDTO.version,
                author = author,
            )

        val saved = repository.save(entity)

        val bucketKey =
            saved.bucketKey
                ?: throw IllegalStateException("Snippet was saved but has no bucket key")

        assetServiceClient.createOrUpdateAsset(
            container = saved.bucketContainer,
            key = bucketKey,
            content = requestDTO.content ?: "",
        )

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

        // Validar que el requester es el owner, TODO esto hay que cambiarlo luego
        if (entity.ownerId != requesterId) {
            throw IllegalAccessException("You don't have permission to access this snippet")
        }

        return entity.toDto()
    }

    override fun getOwnerSnippets(
        ownerId: String,
        page: Int,
        pageSize: Int,
    ): PaginatedSnippetsDTO {
        val safePage = if (page < 0) 0 else page
        val safeSize = if (pageSize <= 0) 20 else pageSize

        val pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"))

        val pageResult = repository.findByOwnerId(ownerId, pageable)

        return PaginatedSnippetsDTO(
            page = pageResult.number,
            pageSize = pageResult.size,
            count = pageResult.totalElements,
            snippets = pageResult.content.map { it.toDto() },
        )
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

        if (existing.ownerId != requesterId) {
            throw IllegalAccessException("You don't have permission to update this snippet")
        }

        requestDTO.name?.let { existing.name = it }
        requestDTO.description?.let { existing.description = it }
        requestDTO.language?.let { existing.language = it }
        requestDTO.version?.let { existing.version = it }

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

        if (existing.ownerId != requesterId) {
            throw IllegalAccessException("You don't have permission to delete this snippet")
        }

        val bucketKey =
            existing.bucketKey ?: throw IllegalStateException("Snippet has no bucket key")

        assetServiceClient.deleteAsset(
            container = existing.bucketContainer,
            key = bucketKey,
        )

        repository.deleteById(id)
    }

    private fun Snippet.toDto() =
        SnippetResponseDTO(
            snippetId = this.id ?: 0L,
            name = this.name,
            description = this.description,
            language = this.language,
            version = this.version,
            author = this.author,
            content = assetServiceClient.getAsset(this.bucketContainer, this.bucketKey!!),
        )
}
