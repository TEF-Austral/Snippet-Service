package repositories

import entities.Snippet
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SnippetRepository : JpaRepository<Snippet, Long> {

    fun findByBucketId(bucketId: String): List<Snippet>

    fun getOwnerSnippets(ownerId: String): List<Snippet>

    fun getAllSnippetThatUserHasAccess(userId: String): List<Snippet>
}
