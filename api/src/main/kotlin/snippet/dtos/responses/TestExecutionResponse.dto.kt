package snippet.dtos.responses

data class TestExecutionResponseDTO(
    val result: String,
    val output: List<String>,
    val errors: List<String>,
)
