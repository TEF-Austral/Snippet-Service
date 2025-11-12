package controllers

import common.dtos.requests.ValidateContentRequestDTO
import dtos.responses.ValidationResponseDTO
import component.PrintScriptServiceClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/snippets")
class ValidateContentController(
    private val printScriptServiceClient: PrintScriptServiceClient,
) {
    @PostMapping("/validate-content")
    fun validateContent(
        @RequestBody request: ValidateContentRequestDTO,
    ): ResponseEntity<ValidationResponseDTO> {
        val result =
            printScriptServiceClient.validateContent(
                content = request.content,
                version = request.version,
            )

        return ResponseEntity.ok(result)
    }
}
