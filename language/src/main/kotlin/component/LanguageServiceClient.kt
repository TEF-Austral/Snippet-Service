package component

import dtos.requests.CreateTestRequestDTO
import dtos.responses.TestDTO
import dtos.responses.TestExecutionResponseDTO
import dtos.responses.ValidationResponseDTO
import language.LanguageServiceClientInt
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class LanguageServiceClient(
    private val restTemplate: RestTemplate,
    @param:Value("\${printscript.service.url}") private val printScriptServiceUrl: String,
) : LanguageServiceClientInt {

    override fun analyzeSnippet(
        container: String,
        key: String,
        version: String,
        userId: String,
        language: String,
    ): ValidationResponseDTO {
        val url =
            "$printScriptServiceUrl/analyze?container" +
                "=$container&key=$key&version=$version&userId=$userId&language=$language"

        return restTemplate.getForObject(
            url,
            ValidationResponseDTO::class.java,
        ) ?: throw IllegalStateException("Failed to validate snippet")
    }

    override fun compileSnippet(
        container: String,
        key: String,
        version: String,
        language: String,
    ): ValidationResponseDTO {
        val url =
            "$printScriptServiceUrl/analyze" +
                "/compile?container=$container&key=$key&version=$version&language=$language"

        return restTemplate.getForObject(url, ValidationResponseDTO::class.java)
            ?: throw IllegalStateException("Failed to compile snippet")
    }

    override fun formatSnippet(
        container: String,
        key: String,
        version: String,
        userId: String,
        language: String,
    ): String {
        val url =
            "$printScriptServiceUrl/format?container" +
                "=$container&key=$key&version=$version&userId=$userId&language=$language"

        return restTemplate.postForObject(url, null, String::class.java)
            ?: throw IllegalStateException("Failed to format snippet")
    }

    override fun previewFormat(
        container: String,
        key: String,
        version: String,
        userId: String,
        language: String,
    ): String {
        val url =
            "$printScriptServiceUrl/format" +
                "/preview?container=$container&key=$key&version=$version&userId=$userId&language=$language"

        return restTemplate.postForObject(url, null, String::class.java)
            ?: throw IllegalStateException("Failed to preview format")
    }

    override fun executeTest(
        container: String,
        key: String,
        version: String,
        testId: Long,
        language: String,
    ): TestExecutionResponseDTO {
        val url =
            "$printScriptServiceUrl/tests" +
                "/execute?container=$container&key=$key&version=$version&testId=$testId&language=$language"

        return restTemplate.postForObject(url, null, TestExecutionResponseDTO::class.java)
            ?: throw IllegalStateException("Failed to execute test")
    }

    override fun downloadFormatted(
        container: String,
        key: String,
        version: String,
        language: String,
    ): ByteArray {
        val url =
            "$printScriptServiceUrl/download" +
                "/formatted?container=$container&key=$key&version=$version&language=$language"

        return restTemplate.postForObject(url, null, ByteArray::class.java)
            ?: throw IllegalStateException("Failed to download formatted content")
    }

    override fun createTestCase(request: CreateTestRequestDTO): TestDTO {
        val url = "$printScriptServiceUrl/tests"

        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }
        val requestEntity = HttpEntity(request, headers)

        val response = restTemplate.postForEntity(url, requestEntity, TestDTO::class.java)
        return response.body ?: throw IllegalStateException("Failed to create test case")
    }

    override fun getTestsBySnippet(snippetId: Long): List<TestDTO> {
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

    override fun getTestById(id: Long): TestDTO {
        val url = "$printScriptServiceUrl/tests/$id"

        return restTemplate.getForObject(url, TestDTO::class.java)
            ?: throw NoSuchElementException("Test not found: $id")
    }

    override fun deleteTestCase(id: Long) {
        val url = "$printScriptServiceUrl/tests/$id"
        restTemplate.delete(url)
    }

    override fun validateContent(
        content: String,
        version: String,
        language: String,
    ): ValidationResponseDTO {
        val url = "$printScriptServiceUrl/analyze/validate?version=$version&language=$language"

        val headers =
            HttpHeaders().apply {
                contentType = MediaType.TEXT_PLAIN
            }

        val request = HttpEntity(content, headers)

        return restTemplate.postForObject(url, request, ValidationResponseDTO::class.java)
            ?: throw IllegalStateException("Failed to validate content")
    }

    override fun updateTestCase(
        id: Long,
        request: CreateTestRequestDTO,
    ) {
        val url = "$printScriptServiceUrl/tests/$id"
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }
        val requestEntity = HttpEntity(request, headers)
        restTemplate.put(url, requestEntity)
    }
}
