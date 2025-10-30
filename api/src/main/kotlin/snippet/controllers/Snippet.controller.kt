package snippet.controllers

import common.Language
import snippet.dtos.SnippetResponseDTO
import snippet.dtos.UpdateSnippetDTO
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import snippet.dtos.CreateSnippetDTO
import snippet.security.AuthenticatedUserProvider
import snippet.services.SnippetService

@RestController
@RequestMapping("/snippets")
class SnippetController(
    private val service: SnippetService,
    private val authenticatedUserProvider: AuthenticatedUserProvider,
) {

    @PostMapping("/", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createSnippet(
        @RequestPart("name") name: String,
        @RequestPart("description") description: String,
        @RequestPart("language") language: String,
        @RequestPart("version") version: String,
        @RequestPart(name = "content", required = false) content: String?,
        @RequestPart(name = "file", required = false) file: MultipartFile?,
    ): ResponseEntity<SnippetResponseDTO> {
        val userId = authenticatedUserProvider.getCurrentUserId()

        // Priorizar archivo sobre content text
        val finalContent =
            when {
                file != null -> String(file.bytes, Charsets.UTF_8)
                content != null -> content
                else -> ""
            }

        val requestDTO =
            CreateSnippetDTO(
                name = name,
                description = description,
                language = Language.valueOf(language.uppercase()),
                version = version,
                content = finalContent,
            )

        val created = service.createSnippet(requestDTO, userId)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @DeleteMapping("/{id}")
    fun deleteSnippet(
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        service.deleteSnippet(id, userId) // Validar ownership
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/{id}")
    fun updateSnippet(
        @PathVariable id: Long,
        @Valid @RequestBody requestDTO: UpdateSnippetDTO,
    ): ResponseEntity<SnippetResponseDTO> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        val updated = service.updateSnippet(id, requestDTO, userId)
        return ResponseEntity.ok(updated)
    }

    @GetMapping("/{id}")
    fun getSnippet(
        @PathVariable id: Long,
    ): ResponseEntity<SnippetResponseDTO> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        val snippet = service.getSnippetById(id, userId)
        return ResponseEntity.ok(snippet)
    }

    @GetMapping("/")
    fun getMySnippets(): ResponseEntity<List<SnippetResponseDTO>> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        val snippets = service.getOwnerSnippets(userId)
        return ResponseEntity.ok(snippets)
    }
}
