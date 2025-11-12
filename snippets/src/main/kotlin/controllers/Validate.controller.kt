package controllers

import dtos.responses.ValidationResponseDTO
import dtos.requests.ValidateContentRequestDTO
import language.ExecutionServiceClientInt
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/snippets")
class ValidateContentController(
    private val executionServiceClient: ExecutionServiceClientInt,
) {
    @PostMapping("/validate-content")
    fun validateContent(
        @RequestBody request: ValidateContentRequestDTO,
    ): ResponseEntity<ValidationResponseDTO> {
        val result =
            executionServiceClient.validateContent(
                content = request.content,
                version = request.version,
            )

        return ResponseEntity.ok(result)
    }
}
