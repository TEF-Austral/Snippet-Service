package common.dtos.requests

data class CreateTestRequestDTO(
    val snippetId: Long,
    val name: String,
    val inputs: List<String>?,
    val expectedOutputs: List<String>?,
)
