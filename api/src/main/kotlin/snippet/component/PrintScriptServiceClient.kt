package snippet.component

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import snippet.dtos.FormatConfigDTO
import snippet.dtos.TestDTO
import snippet.dtos.requests.CreateTestRequestDTO
import snippet.dtos.responses.TestExecutionResponseDTO
import snippet.dtos.responses.ValidationResponseDTO

@Component
class PrintScriptServiceClient(
    private val restTemplate: RestTemplate,
    @param:Value("\${printscript.service.url}") private val printScriptServiceUrl: String,
) {

    fun validateSnippet(
        container: String,
        key: String,
        version: String,
        userId: String,
    ): ValidationResponseDTO {
        val uri =
            UriComponentsBuilder
                .fromHttpUrl("$printScriptServiceUrl/analyze")
                .queryParam("container", container)
                .queryParam("key", key)
                .queryParam("version", version)
                .queryParam("userId", userId)
                .toUriString()

        return restTemplate.getForObject(
            uri,
            ValidationResponseDTO::class.java,
        )
            ?: throw IllegalStateException("Failed to validate snippet")
    }

    fun compileSnippet(
        container: String,
        key: String,
        version: String,
    ): ValidationResponseDTO {
        val uri =
            UriComponentsBuilder
                .fromHttpUrl("$printScriptServiceUrl/analyze/compile")
                .queryParam("container", container)
                .queryParam("key", key)
                .queryParam("version", version)
                .toUriString()

        return restTemplate.getForObject(uri, ValidationResponseDTO::class.java)
            ?: throw IllegalStateException("Failed to compile snippet")
    }

    fun formatSnippet(
        container: String,
        key: String,
        version: String,
        config: FormatConfigDTO,
        userId: String,
    ): String {
        val uri =
            UriComponentsBuilder
                .fromHttpUrl("$printScriptServiceUrl/format")
                .queryParam("container", container)
                .queryParam("key", key)
                .queryParam("version", version)
                .queryParam("userId", userId)
                .toUriString()

        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }
        val request = HttpEntity(config, headers)

        return restTemplate.postForObject(uri, request, String::class.java)
            ?: throw IllegalStateException("Failed to format snippet")
    }

    fun previewFormat(
        container: String,
        key: String,
        version: String,
        config: FormatConfigDTO,
        userId: String,
    ): String {
        val uri =
            UriComponentsBuilder
                .fromHttpUrl("$printScriptServiceUrl/format/preview")
                .queryParam("container", container)
                .queryParam("key", key)
                .queryParam("version", version)
                .queryParam("userId", userId)
                .toUriString()

        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }
        val request = HttpEntity(config, headers)

        return restTemplate.postForObject(uri, request, String::class.java)
            ?: throw IllegalStateException("Failed to preview format")
    }

    fun executeTest(
        container: String,
        key: String,
        version: String,
        testId: Long,
        userId: String,
    ): TestExecutionResponseDTO {
        val uri =
            UriComponentsBuilder
                .fromHttpUrl("$printScriptServiceUrl/tests/execute")
                .queryParam("container", container)
                .queryParam("key", key)
                .queryParam("version", version)
                .queryParam("testId", testId)
                .queryParam("userId", userId)
                .toUriString()

        return restTemplate.postForObject(uri, null, TestExecutionResponseDTO::class.java)
            ?: throw IllegalStateException("Failed to execute test")
    }

    fun downloadFormatted(
        container: String,
        key: String,
        version: String,
        config: FormatConfigDTO,
    ): ByteArray {
        val uri =
            UriComponentsBuilder
                .fromHttpUrl("$printScriptServiceUrl/download/formatted")
                .queryParam("container", container)
                .queryParam("key", key)
                .queryParam("version", version)
                .toUriString()

        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }
        val request = HttpEntity(config, headers)

        return restTemplate.postForObject(uri, request, ByteArray::class.java)
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
        val uri =
            UriComponentsBuilder
                .fromHttpUrl("$printScriptServiceUrl/tests")
                .queryParam("snippetId", snippetId)
                .toUriString()

        val response =
            restTemplate.exchange(
                uri,
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
        language: String,
        version: String,
    ): ValidationResponseDTO {
        val url = "$printScriptServiceUrl/analyze/validate"

        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

        val requestBody =
            mapOf(
                "content" to content,
                "language" to language,
                "version" to version,
            )

        val request = HttpEntity(requestBody, headers)

        return restTemplate.postForObject(url, request, ValidationResponseDTO::class.java)
            ?: throw IllegalStateException("Failed to validate content")
    }
}
