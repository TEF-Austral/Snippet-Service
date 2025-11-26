package controllers

import dtos.requests.ValidateContentRequestDTO
import dtos.responses.ValidationResponseDTO
import language.LanguageServiceClientInt
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/snippets")
class ValidateContentController(
    private val executionServiceClient: LanguageServiceClientInt,
) {
    private val log = org.slf4j.LoggerFactory.getLogger(ValidateContentController::class.java)

    @PostMapping("/validate-content")
    fun validateContent(
        @RequestBody request: ValidateContentRequestDTO,
    ): ResponseEntity<ValidationResponseDTO> {
        log.info(
            "POST /snippets/validate-content - Validating content, version ${request.version}, language ${request.language}",
        )
        val result =
            executionServiceClient.validateContent(
                content = request.content,
                version = request.version,
                language = request.language,
            )

        log.warn("POST /snippets/validate-content - Content validated, isValid: ${result.isValid}")
        return ResponseEntity.ok(result)
    }
}
