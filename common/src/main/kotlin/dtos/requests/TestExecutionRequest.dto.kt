package common.dtos.requests

data class TestExecutionRequestDTO(
    val snippetId: Long,
    val version: String,
    val testId: Long,
)
