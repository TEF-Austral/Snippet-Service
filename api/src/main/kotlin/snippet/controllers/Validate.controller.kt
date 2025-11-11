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
    private val log = org.slf4j.LoggerFactory.getLogger(ValidateContentController::class.java)

    @PostMapping("/validate-content")
    fun validateContent(
        @RequestBody request: ValidateContentRequestDTO,
    ): ResponseEntity<ValidationResponseDTO> {
        log.info("POST /snippets/validate-content - Validating content, version ${request.version}")
        val result =
            printScriptServiceClient.validateContent(
                content = request.content,
                version = request.version,
            )

        log.warn("POST /snippets/validate-content - Content validated, isValid: ${result.isValid}")
        return ResponseEntity.ok(result)
    }
}
