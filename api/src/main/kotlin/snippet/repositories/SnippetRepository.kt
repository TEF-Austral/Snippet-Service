package snippet.repositories

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import snippet.entities.Snippet

@Repository
interface SnippetRepository : JpaRepository<Snippet, Long> {

    fun findByBucketKey(bucketKey: String): List<Snippet>

    fun findByOwnerId(
        ownerId: String,
        pageable: Pageable,
    ): Page<Snippet>
}
