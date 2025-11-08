package snippet.component

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import snippet.dtos.TestDTO
import snippet.dtos.requests.CreateTestRequestDTO
import snippet.dtos.responses.TestExecutionResponseDTO
import snippet.dtos.responses.ValidationResponseDTO

@Component
class PrintScriptServiceClient(
    private val restTemplate: RestTemplate,
    @param:Value("\${printscript.service.url}") private val printScriptServiceUrl: String,
) {

    fun analyzeSnippet(
        container: String,
        key: String,
        version: String,
        userId: String,
    ): ValidationResponseDTO {
        val url =
            "$printScriptServiceUrl/analyze?container" +
                "=$container&key=$key&version=$version&userId=$userId"

        return restTemplate.getForObject(
            url,
            ValidationResponseDTO::class.java,
        )
            ?: throw IllegalStateException("Failed to validate snippet")
    }

    fun compileSnippet(
        container: String,
        key: String,
        version: String,
    ): ValidationResponseDTO {
        val url =
            "$printScriptServiceUrl/analyze" +
                "/compile?container=$container&key=$key&version=$version"

        return restTemplate.getForObject(url, ValidationResponseDTO::class.java)
            ?: throw IllegalStateException("Failed to compile snippet")
    }

    fun formatSnippet(
        container: String,
        key: String,
        version: String,
        userId: String,
    ): String {
        val url =
            "$printScriptServiceUrl/format?container" +
                "=$container&key=$key&version=$version&userId=$userId"

        return restTemplate.postForObject(url, null, String::class.java)
            ?: throw IllegalStateException("Failed to format snippet")
    }

    fun previewFormat(
        container: String,
        key: String,
        version: String,
        userId: String,
    ): String {
        val url =
            "$printScriptServiceUrl/format" +
                "/preview?container=$container&key=$key&version=$version&userId=$userId"

        return restTemplate.postForObject(url, null, String::class.java)
            ?: throw IllegalStateException("Failed to preview format")
    }

    fun executeTest(
        container: String,
        key: String,
        version: String,
        testId: Long,
    ): TestExecutionResponseDTO {
        val url =
            "$printScriptServiceUrl/tests" +
                "/execute?container=$container&key=$key&version=$version&testId=$testId"

        return restTemplate.postForObject(url, null, TestExecutionResponseDTO::class.java)
            ?: throw IllegalStateException("Failed to execute test")
    }

    fun downloadFormatted(
        container: String,
        key: String,
        version: String,
    ): ByteArray {
        val url =
            "$printScriptServiceUrl/download" +
                "/formatted?container=$container&key=$key&version=$version"

        return restTemplate.postForObject(url, null, ByteArray::class.java)
            ?: throw IllegalStateException("Failed to download formatted content")
    }

    fun createTestCase(request: CreateTestRequestDTO): TestDTO {
        val url = "$printScriptServiceUrl/tests"

        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }
        val requestEntity = HttpEntity(request, headers)

        val response = restTemplate.postForEntity(url, requestEntity, TestDTO::class.java)
        return response.body ?: throw IllegalStateException("Failed to create test case")
    }

    fun getTestsBySnippet(snippetId: Long): List<TestDTO> {
        val url = "$printScriptServiceUrl/tests?snippetId=$snippetId"

        val response =
            restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                Array<TestDTO>::class.java,
            )

        return response.body?.toList() ?: emptyList()
    }

    fun getTestById(id: Long): TestDTO {
        val url = "$printScriptServiceUrl/tests/$id"

        return restTemplate.getForObject(url, TestDTO::class.java)
            ?: throw NoSuchElementException("Test not found: $id")
    }

    fun deleteTestCase(id: Long) {
        val url = "$printScriptServiceUrl/tests/$id"
        restTemplate.delete(url)
    }

    fun validateContent(
        content: String,
        version: String,
    ): ValidationResponseDTO {
        val url = "$printScriptServiceUrl/analyze/validate?version=$version"

        val headers =
            HttpHeaders().apply {
                contentType = MediaType.TEXT_PLAIN
            }

        val request = HttpEntity(content, headers)

        return restTemplate.postForObject(url, request, ValidationResponseDTO::class.java)
            ?: throw IllegalStateException("Failed to validate content")
    }
}
