package language

import common.dtos.requests.CreateTestRequestDTO
import common.dtos.responses.TestDTO
import common.dtos.responses.TestExecutionResponseDTO
import dtos.responses.ValidationResponseDTO

interface LanguageServiceClient {

    fun analyzeSnippet(
        container: String,
        key: String,
        version: String,
        userId: String,
    ): ValidationResponseDTO

    fun compileSnippet(
        container: String,
        key: String,
        version: String,
    ): ValidationResponseDTO

    fun formatSnippet(
        container: String,
        key: String,
        version: String,
        userId: String,
    ): String

    fun previewFormat(
        container: String,
        key: String,
        version: String,
        userId: String,
    ): String

    fun executeTest(
        container: String,
        key: String,
        version: String,
        testId: Long,
    ): TestExecutionResponseDTO

    fun downloadFormatted(
        container: String,
        key: String,
        version: String,
    ): ByteArray

    fun createTestCase(request: CreateTestRequestDTO): TestDTO

    fun getTestsBySnippet(snippetId: Long): List<TestDTO>

    fun getTestById(id: Long): TestDTO

    fun deleteTestCase(id: Long)

    fun validateContent(
        content: String,
        version: String,
    ): ValidationResponseDTO

    fun updateTestCase(
        id: Long,
        request: CreateTestRequestDTO,
    )
}
