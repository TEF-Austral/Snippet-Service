package api.repositories

import api.entities.SnippetEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SnippetRepository : JpaRepository<SnippetEntity, Long> {
    fun findByIdAndDeletedAtIsNull(id: Long): SnippetEntity?

    fun findAllByDeletedAtIsNull(): List<SnippetEntity>
}
