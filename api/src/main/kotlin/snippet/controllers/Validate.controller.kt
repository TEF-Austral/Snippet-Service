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
@RequestMapping("/validate")
class ValidateContentController(
    private val printScriptServiceClient: PrintScriptServiceClient,
) {

    @PostMapping("/content")
    fun validateContent(
        @RequestBody request: ValidateContentRequestDTO,
    ): ResponseEntity<ValidationResponseDTO> {
        val result =
            printScriptServiceClient.validateContent(
                content = request.content,
                language = request.language,
                version = request.version,
            )

        return ResponseEntity.ok(result)
    }
}
