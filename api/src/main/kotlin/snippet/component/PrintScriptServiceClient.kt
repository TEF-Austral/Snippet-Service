package snippet.component

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import snippet.dtos.AnalyzerRuleDTO
import snippet.dtos.FormatConfigDTO
import snippet.dtos.FormatterRuleDTO
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
    ): ValidationResponseDTO {
        val uri =
            UriComponentsBuilder
                .fromHttpUrl("$printScriptServiceUrl/analyze")
                .queryParam("container", container)
                .queryParam("key", key)
                .queryParam("version", version)
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

        return restTemplate.postForObject(uri, null, TestExecutionResponseDTO::class.java)
            ?: throw IllegalStateException("Failed to execute test")
    }

    // ============================================================================
    // MÉTODOS MODIFICADOS - Agregar parámetro userId
    // ============================================================================

    fun getFormatterConfig(userId: String): List<FormatterRuleDTO> {
        val uri =
            UriComponentsBuilder
                .fromHttpUrl("$printScriptServiceUrl/config/format")
                .queryParam("userId", userId) // ✅ Agregar userId como query param
                .toUriString()

        val response = restTemplate.getForObject(uri, Array<FormatterRuleDTO>::class.java)
        return response?.toList() ?: emptyList()
    }

    fun updateFormatterConfig(
        userId: String,
        rules: List<FormatterRuleDTO>,
    ): List<FormatterRuleDTO> {
        val uri =
            UriComponentsBuilder
                .fromHttpUrl("$printScriptServiceUrl/config/update/format")
                .queryParam("userId", userId) // ✅ Agregar userId como query param
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
                org.springframework.http.HttpMethod.PUT,
                request,
                Array<FormatterRuleDTO>::class.java,
            )

        return response.body?.toList() ?: emptyList()
    }

    fun getAnalyzerConfig(userId: String): List<AnalyzerRuleDTO> {
        val uri =
            UriComponentsBuilder
                .fromHttpUrl("$printScriptServiceUrl/config/analyze")
                .queryParam("userId", userId) // ✅ Agregar userId como query param
                .toUriString()

        val response = restTemplate.getForObject(uri, Array<AnalyzerRuleDTO>::class.java)
        return response?.toList() ?: emptyList()
    }

    fun updateAnalyzerConfig(
        userId: String,
        rules: List<AnalyzerRuleDTO>,
    ): List<AnalyzerRuleDTO> {
        val uri =
            UriComponentsBuilder
                .fromHttpUrl("$printScriptServiceUrl/config/update/analyze")
                .queryParam("userId", userId) // ✅ Agregar userId como query param
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
                org.springframework.http.HttpMethod.PUT,
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
