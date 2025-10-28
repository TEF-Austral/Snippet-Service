package controllers

import dtos.SnippetRequestDTO
import dtos.SnippetResponseDTO
import dtos.UpdateSnippetDTO
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
import org.springframework.web.bind.annotation.RestController
import security.AuthenticatedUserProvider
import services.SnippetService

@RestController
@RequestMapping("/snippets")
class SnippetController(
    private val service: SnippetService,
    private val authenticatedUserProvider: AuthenticatedUserProvider,
) {

    @PostMapping("/")
    fun createSnippet(
        @Valid @RequestBody requestDTO: SnippetRequestDTO,
    ): ResponseEntity<SnippetResponseDTO> {
        val userId = authenticatedUserProvider.getCurrentUserId()
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
