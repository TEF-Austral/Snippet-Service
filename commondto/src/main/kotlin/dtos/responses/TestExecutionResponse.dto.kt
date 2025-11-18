package dtos.responses

data class TestExecutionResponseDTO(
    val testId: Long,
    val passed: Boolean,
    val outputs: List<String>,
    val expectedOutputs: List<String>,
    val errors: List<String>,
)
