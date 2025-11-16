package repositories

import entity.TestResult
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TestResultRepository : JpaRepository<TestResult, Long> {
    fun findBySnippetIdOrderByExecutedAtDesc(snippetId: Long): List<TestResult>

    fun findTop10BySnippetIdOrderByExecutedAtDesc(snippetId: Long): List<TestResult>
}
