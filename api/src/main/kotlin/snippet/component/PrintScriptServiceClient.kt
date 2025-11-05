package snippet.component

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject
import org.springframework.web.util.UriComponentsBuilder
import snippet.security.AuthenticatedUserProvider

@Component
class PrintScriptServiceClient(
    private val restTemplate: RestTemplate,
    private val authenticatedUserProvider: AuthenticatedUserProvider,
    @param:Value("\${printscript.service.url}") private val printScriptServiceUrl: String,
) {

    fun validateSnippet(
        container: String,
        key: String,
        version: String,
    ): ValidationResponse {
        val userId = authenticatedUserProvider.getCurrentUserId()

        val uriBuilder =
            UriComponentsBuilder
                .fromHttpUrl("$printScriptServiceUrl/analyze")
                .queryParam("container", container)
                .queryParam("key", key)
                .queryParam("version", version)
                .queryParam("userId", userId)

        return restTemplate.getForObject(uriBuilder.toUriString(), ValidationResponse::class.java)
            ?: throw IllegalStateException("Failed to validate snippet")
    }

    fun compileSnippet(
        container: String,
        key: String,
        version: String,
    ): ValidationResponse {
        val uri =
            UriComponentsBuilder
                .fromHttpUrl("$printScriptServiceUrl/analyze/compile")
                .queryParam("container", container)
                .queryParam("key", key)
                .queryParam("version", version)
                .toUriString()

        return restTemplate.getForObject(uri, ValidationResponse::class.java)
            ?: throw IllegalStateException("Failed to compile snippet")
    }

    fun formatSnippet(
        container: String,
        key: String,
        version: String,
        config: FormatConfigDTO,
    ): String {
        val uri =
            UriComponentsBuilder
                .fromHttpUrl("$printScriptServiceUrl/format")
                .queryParam("container", container)
                .queryParam("key", key)
                .queryParam("version", version)
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
    ): String {
        val uri =
            UriComponentsBuilder
                .fromHttpUrl("$printScriptServiceUrl/format/preview")
                .queryParam("container", container)
                .queryParam("key", key)
                .queryParam("version", version)
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
    ): TestExecutionResponseDTO {
        val uri =
            UriComponentsBuilder
                .fromHttpUrl("$printScriptServiceUrl/tests/execute")
                .queryParam("container", container)
                .queryParam("key", key)
                .queryParam("version", version)
                .queryParam("testId", testId)
                .toUriString()

        return restTemplate.postForObject(uri, TestExecutionResponseDTO::class.java)
            ?: throw IllegalStateException("Failed to execute test")
    }

    fun getFormatterConfig(): List<FormatterRuleDTO> {
        val userId = authenticatedUserProvider.getCurrentUserId()

        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

        val requestBody = mapOf("userId" to userId)
        val request = HttpEntity(requestBody, headers)

        val response =
            restTemplate.exchange(
                "$printScriptServiceUrl/config/format",
                HttpMethod.POST,
                request,
                Array<FormatterRuleDTO>::class.java,
            )

        return response.body?.toList() ?: emptyList()
    }

    fun updateFormatterConfig(rules: List<FormatterRuleDTO>): List<FormatterRuleDTO> {
        val userId = authenticatedUserProvider.getCurrentUserId()

        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

        val requestBody =
            mapOf(
                "userId" to userId,
                "rules" to rules,
            )
        val request = HttpEntity(requestBody, headers)

        val response =
            restTemplate.exchange(
                "$printScriptServiceUrl/config/format",
                HttpMethod.PUT,
                request,
                Array<FormatterRuleDTO>::class.java,
            )

        return response.body?.toList() ?: emptyList()
    }

    fun getAnalyzerConfig(): List<AnalyzerRuleDTO> {
        val userId = authenticatedUserProvider.getCurrentUserId()

        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

        val requestBody = mapOf("userId" to userId)
        val request = HttpEntity(requestBody, headers)

        val response =
            restTemplate.exchange(
                "$printScriptServiceUrl/config/analyze",
                HttpMethod.POST,
                request,
                Array<AnalyzerRuleDTO>::class.java,
            )

        return response.body?.toList() ?: emptyList()
    }

    fun updateAnalyzerConfig(rules: List<AnalyzerRuleDTO>): List<AnalyzerRuleDTO> {
        val userId = authenticatedUserProvider.getCurrentUserId()

        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

        val requestBody =
            mapOf(
                "userId" to userId,
                "rules" to rules,
            )
        val request = HttpEntity(requestBody, headers)

        val response =
            restTemplate.exchange(
                "$printScriptServiceUrl/config/analyze",
                HttpMethod.PUT,
                request,
                Array<AnalyzerRuleDTO>::class.java,
            )

        return response.body?.toList() ?: emptyList()
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
}

data class ValidationResponse(
    val isValid: Boolean,
    val violations: List<LintViolation>,
)

data class LintViolation(
    val message: String,
    val line: Int,
    val column: Int,
)

data class FormatConfigDTO(
    val spaceBeforeColon: Boolean,
    val spaceAfterColon: Boolean,
    val spacesInAssignation: Int,
    val newLineBeforePrintln: Int,
)

data class FormatterRuleDTO(
    val id: Long?,
    val name: String,
    val isActive: Boolean,
    val value: String?,
)

data class AnalyzerRuleDTO(
    val id: Long?,
    val name: String,
    val isActive: Boolean,
)

data class TestExecutionResponseDTO(
    val result: String,
    val output: List<String>,
    val errors: List<String>,
)
