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
import snippet.dtos.AnalyzerRuleDTO
import snippet.dtos.FormatConfigDTO
import snippet.dtos.FormatterRuleDTO
import snippet.dtos.responses.TestExecutionResponseDTO
import snippet.dtos.responses.ValidationResponseDTO
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
    ): ValidationResponseDTO {
        val userId = authenticatedUserProvider.getCurrentUserId()

        val uriBuilder =
            UriComponentsBuilder
                .fromHttpUrl("$printScriptServiceUrl/analyze")
                .queryParam("container", container)
                .queryParam("key", key)
                .queryParam("version", version)
                .queryParam("userId", userId)

        return restTemplate.getForObject(
            uriBuilder.toUriString(),
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

        val uri =
            UriComponentsBuilder
                .fromHttpUrl("$printScriptServiceUrl/config/format")
                .queryParam("userId", userId)
                .toUriString()

        val response =
            restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                Array<FormatterRuleDTO>::class.java,
            )

        return response.body?.toList() ?: emptyList()
    }

    fun updateFormatterConfig(rules: List<FormatterRuleDTO>): List<FormatterRuleDTO> {
        val userId = authenticatedUserProvider.getCurrentUserId()

        val uri =
            UriComponentsBuilder
                .fromHttpUrl("$printScriptServiceUrl/config/format")
                .queryParam("userId", userId)
                .toUriString()

        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

        val requestBody = mapOf("rules" to rules)
        val request = HttpEntity(requestBody, headers)

        val response =
            restTemplate.exchange(
                uri,
                HttpMethod.PUT,
                request,
                Array<FormatterRuleDTO>::class.java,
            )

        return response.body?.toList() ?: emptyList()
    }

    fun getAnalyzerConfig(): List<AnalyzerRuleDTO> {
        val userId = authenticatedUserProvider.getCurrentUserId()

        val uri =
            UriComponentsBuilder
                .fromHttpUrl("$printScriptServiceUrl/config/analyze")
                .queryParam("userId", userId)
                .toUriString()

        val response =
            restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                Array<AnalyzerRuleDTO>::class.java,
            )

        return response.body?.toList() ?: emptyList()
    }

    fun updateAnalyzerConfig(rules: List<AnalyzerRuleDTO>): List<AnalyzerRuleDTO> {
        val userId = authenticatedUserProvider.getCurrentUserId()

        val uri =
            UriComponentsBuilder
                .fromHttpUrl("$printScriptServiceUrl/config/analyze")
                .queryParam("userId", userId)
                .toUriString()

        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

        val requestBody = mapOf("rules" to rules)
        val request = HttpEntity(requestBody, headers)

        val response =
            restTemplate.exchange(
                uri,
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
