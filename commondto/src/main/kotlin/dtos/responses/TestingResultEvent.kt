package dtos.responses

data class TestingResultEvent(
    val requestId: String,
    val testId: Long,
    val snippetId: Long,
    val passed: Boolean,
    val outputs: List<String> = emptyList(),
    val expectedOutputs: List<String> = emptyList(),
    val errors: List<String> = emptyList(),
)
