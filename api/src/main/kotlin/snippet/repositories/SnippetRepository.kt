package snippet.repositories

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import snippet.entities.Snippet

@Repository
interface SnippetRepository :
    JpaRepository<Snippet, Long>,
    JpaSpecificationExecutor<Snippet> {

    fun findByOwnerIdOrIdIn(
        ownerId: String,
        ids: List<Long>,
        pageable: Pageable,
    ): Page<Snippet>

    fun findByOwnerId(
        ownerId: String,
        pageable: Pageable,
    ): Page<Snippet>

    fun findByIdIn(
        snippetIds: List<Long>,
        pageable: Pageable,
    ): Page<Snippet>
}
