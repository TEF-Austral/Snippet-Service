package snippet.controllers

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import snippet.component.PrintScriptServiceClient
import snippet.dtos.requests.ValidateContentRequestDTO
import snippet.dtos.responses.ValidationResponseDTO

@RestController
@RequestMapping("/snippets")
class ValidateContentController(
    private val printScriptServiceClient: PrintScriptServiceClient,
) {

    private fun normalizeVersion(version: String): String =
        when (version) {
            "1.1.0" -> "1.1"
            "1.0.0" -> "1.0"
            else -> version
        }

    @PostMapping("/validate-content")
    fun validateContent(
        @RequestBody request: ValidateContentRequestDTO,
    ): ResponseEntity<ValidationResponseDTO> {
        val normalizedVersion = normalizeVersion(request.version)

        val result =
            printScriptServiceClient.validateContent(
                content = request.content,
                version = normalizedVersion,
            )

        return ResponseEntity.ok(result)
    }
}
