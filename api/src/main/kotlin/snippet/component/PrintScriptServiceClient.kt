package snippet.component

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Component
class PrintScriptServiceClient(
    private val restTemplate: RestTemplate,
    @param:Value("\${printscript.service.url}") private val printScriptServiceUrl: String,
) {

    fun validateSnippet(
        container: String,
        key: String,
        version: String,
        userId: String?,
    ): ValidationResponse {
        val uriBuilder =
            UriComponentsBuilder
                .fromHttpUrl("$printScriptServiceUrl/analyze")
                .queryParam("container", container)
                .queryParam("key", key)
                .queryParam("version", version)

        userId?.let {
            uriBuilder.queryParam("userId", it)
        }

        return restTemplate.getForObject(uriBuilder.toUriString(), ValidationResponse::class.java)
            ?: throw IllegalStateException("Failed to validate snippet")
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
