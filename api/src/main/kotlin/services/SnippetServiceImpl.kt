package services

import events.SnippetEvent
import events.SnippetEventProducer
import events.SnippetOperation
import component.AssetServiceClient
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
    private val eventProducer: SnippetEventProducer,
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

        // Emitir evento
        eventProducer.publishSnippetEvent(
            SnippetEvent(
                snippetId = saved.id!!,
                bucketId = saved.bucketContainer,
                bucketContainer = saved.bucketContainer,
                ownerId = saved.ownerId,
                name = saved.name,
                content = null,
                language = saved.language,
                version = saved.version,
                operation = SnippetOperation.CREATE,
            ),
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

    override fun getOwnerSnippets(ownerId: String): List<SnippetResponseDTO> =
        repository.findByOwnerId(ownerId).map { it.toDto() }

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

        var content: String? = null
        requestDTO.content?.let { newContent ->
            content = newContent
            assetServiceClient.createOrUpdateAsset(
                container = existing.bucketContainer,
                key =
                    existing.bucketKey
                        ?: throw IllegalStateException("Snippet has no bucket key"),
                content = newContent,
            )
        }

        existing.name = requestDTO.name ?: existing.name
        existing.description = requestDTO.description ?: existing.description
        existing.language = requestDTO.language ?: existing.language
        existing.version = requestDTO.version ?: existing.version

        val saved = repository.save(existing)

        // Emitir evento
        eventProducer.publishSnippetEvent(
            SnippetEvent(
                snippetId = saved.id!!,
                bucketId = saved.bucketContainer,
                bucketContainer = saved.bucketContainer,
                ownerId = saved.ownerId,
                name = saved.name,
                content = content,
                language = saved.language,
                version = saved.version,
                operation = SnippetOperation.UPDATE,
            ),
        )

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

        assetServiceClient.deleteAsset(
            container = existing.bucketContainer,
            key =
                existing.bucketKey
                    ?: throw IllegalStateException("Snippet has no bucket key"),
        )

        repository.deleteById(id)

        // Emitir evento
        eventProducer.publishSnippetEvent(
            SnippetEvent(
                snippetId = existing.id!!,
                bucketId = existing.bucketContainer,
                bucketContainer = existing.bucketContainer,
                ownerId = existing.ownerId,
                name = existing.name,
                content = null,
                language = existing.language,
                version = existing.version,
                operation = SnippetOperation.DELETE,
            ),
        )
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
