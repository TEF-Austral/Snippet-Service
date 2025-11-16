package dtos.responses

import java.time.LocalDateTime

data class SnippetTestSummaryDTO(
    val snippetId: Long,
    val totalTests: Int,
    val passedTests: Int,
    val failedTests: Int,
    val lastExecutedAt: LocalDateTime?,
    val testResults: List<TestResultSummaryDTO>,
)
