package repositories

import entities.Snippet
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SnippetRepository : JpaRepository<Snippet, Long> {

    fun findByBucketKey(bucketKey: String): List<Snippet>

    fun findByOwnerId(ownerId: String): List<Snippet>
}