package dtos.responses

import java.time.LocalDateTime

data class TestResultSummaryDTO(
    val testId: Long,
    val testName: String,
    val passed: Boolean,
    val executedAt: LocalDateTime,
    val outputs: List<String>,
    val expectedOutputs: List<String>,
    val errors: List<String>,
)
