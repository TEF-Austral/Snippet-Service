package dtos.responses

data class TestDTO(
    val id: Long?,
    val snippetId: Long,
    val name: String,
    val inputs: List<String>,
    val expectedOutputs: List<String>,
)
