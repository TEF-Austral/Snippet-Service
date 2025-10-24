package services

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
) : SnippetService {

    override fun createSnippet(requestDTO: SnippetRequestDTO): SnippetResponseDTO {
        val entity =
            Snippet(
                name = requestDTO.name,
                description = requestDTO.description,
                bucketId = "default-bucket",
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
        repository.getOwnerSnippets(ownerId).map {
            it.toDto()
        }

    override fun getSnippetThatUserHasAccess(userId: String): List<SnippetResponseDTO> =
        repository.getAllSnippetThatUserHasAccess(userId).map {
            it.toDto()
        }

    @Transactional
    override fun updateSnippet(
        id: Long,
        requestDTO: UpdateSnippetDTO,
    ): SnippetResponseDTO {
        val existing =
            repository.findById(id).orElseThrow { NoSuchElementException("Snippet not found: $id") }
        existing.name = requestDTO.name ?: existing.name
        existing.description = requestDTO.description ?: existing.description
        existing.language = requestDTO.language ?: existing.language
        existing.version = requestDTO.version ?: existing.version
        val saved = repository.save(existing)
        return saved.toDto()
    }

    override fun deleteSnippet(id: Long) {
        repository.deleteById(id)
    }

    fun deleteAllByBucketId(bucketId: String): Int {
        val snippets = repository.findByBucketId(bucketId)
        val count = snippets.size
        repository.deleteAll(snippets)
        return count
    }

    fun existsByBucketId(bucketId: String): Boolean =
        repository.findByBucketId(bucketId).isNotEmpty()

    fun getAllBucketIds(): List<String> = repository.findAll().map { it.bucketId }.distinct()

    private fun Snippet.toDto() =
        SnippetResponseDTO(
            snippetId = this.snippetId ?: 0L,
            name = this.name,
            description = this.description,
            bucketId = this.bucketId,
            language = this.language,
            version = this.version,
        )
}
