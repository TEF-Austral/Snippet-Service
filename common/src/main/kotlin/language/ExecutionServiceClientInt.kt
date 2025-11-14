package language

import dtos.requests.CreateTestRequestDTO
import dtos.responses.TestDTO
import dtos.responses.TestExecutionResponseDTO
import dtos.responses.ValidationResponseDTO

interface ExecutionServiceClientInt {

    fun analyzeSnippet(
        container: String,
        key: String,
        version: String,
        userId: String,
        language: String,
    ): ValidationResponseDTO

    fun compileSnippet(
        container: String,
        key: String,
        version: String,
        language: String,
    ): ValidationResponseDTO

    fun formatSnippet(
        container: String,
        key: String,
        version: String,
        userId: String,
        language: String,
    ): String

    fun previewFormat(
        container: String,
        key: String,
        version: String,
        userId: String,
        language: String,
    ): String

    fun executeTest(
        container: String,
        key: String,
        version: String,
        testId: Long,
        language: String,
    ): TestExecutionResponseDTO

    fun downloadFormatted(
        container: String,
        key: String,
        version: String,
        language: String,
    ): ByteArray

    fun createTestCase(request: CreateTestRequestDTO): TestDTO

    fun getTestsBySnippet(snippetId: Long): List<TestDTO>

    fun getTestById(id: Long): TestDTO

    fun deleteTestCase(id: Long)

    fun validateContent(
        content: String,
        version: String,
        language: String,
    ): ValidationResponseDTO

    fun updateTestCase(
        id: Long,
        request: CreateTestRequestDTO,
    )
}
