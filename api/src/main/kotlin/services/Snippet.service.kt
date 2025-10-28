package api.services

import api.dtos.CreateSnippetRequest
import api.dtos.SnippetDTO
import api.dtos.UpdateSnippetRequest
import api.entities.SnippetEntity
import api.repositories.SnippetRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class SnippetService(
    private val repository: SnippetRepository,
) {

    fun getAll(): List<SnippetDTO> = repository.findAllByDeletedAtIsNull().map { toDTO(it) }

    fun getById(id: Long): SnippetDTO? =
        repository.findByIdAndDeletedAtIsNull(id)?.let { toDTO(it) }

    fun create(request: CreateSnippetRequest): SnippetDTO {
        val entity = SnippetEntity(name = request.name, code = request.code)
        val saved = repository.save(entity)
        return toDTO(saved)
    }

    fun update(
        id: Long,
        request: UpdateSnippetRequest,
    ): SnippetDTO? {
        val entity = repository.findByIdAndDeletedAtIsNull(id) ?: return null
        entity.name = request.name
        entity.code = request.code
        val updated = repository.save(entity)
        return toDTO(updated)
    }

    fun delete(id: Long): Boolean {
        val entity = repository.findByIdAndDeletedAtIsNull(id) ?: return false
        entity.deletedAt = LocalDateTime.now()
        repository.save(entity)
        return true
    }

    private fun toDTO(entity: SnippetEntity): SnippetDTO =
        SnippetDTO(
            id = entity.id,
            name = entity.name,
            code = entity.code,
            deletedAt = entity.deletedAt,
        )
}
