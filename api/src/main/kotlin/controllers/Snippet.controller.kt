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
import services.SnippetService

@RestController
@RequestMapping("/snippets")
class SnippetController(
    private val service: SnippetService,
) {

    @PostMapping("/")
    fun createSnippet(
        @Valid @RequestBody requestDTO: SnippetRequestDTO,
    ): ResponseEntity<SnippetResponseDTO> {
        val created = service.createSnippet(requestDTO)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @DeleteMapping("/{id}")
    fun deleteSnippet(
        @PathVariable id: Long,
    ): ResponseEntity<SnippetResponseDTO> {
        service.deleteSnippet(id)
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/{id}")
    fun updateSnippet(
        @PathVariable id: Long,
        @Valid @RequestBody requestDTO: UpdateSnippetDTO,
    ): ResponseEntity<SnippetResponseDTO> {
        val updated = service.updateSnippet(id, requestDTO)
        return ResponseEntity.ok(updated)
    }

    @GetMapping("/{id}")
    fun getSnippet(
        @PathVariable id: Long,
    ): ResponseEntity<SnippetResponseDTO> {
        val snippet = service.getSnippetById(id)
        return if (snippet != null) {
            ResponseEntity.ok(snippet)
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }

    // Le falta el middleware que obtenga la idententidad del requester con el token.
    @GetMapping("/")
    fun getSnippetsByUserId(
        @PathVariable userId: String,
    ): ResponseEntity<List<SnippetResponseDTO>> {
        val snippets = service.getOwnerSnippets(userId)
        return ResponseEntity.ok(snippets)
    }
}
