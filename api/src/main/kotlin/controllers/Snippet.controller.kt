package api.controllers

import api.dtos.CreateSnippetRequest
import api.dtos.SnippetDTO
import api.dtos.UpdateSnippetRequest
import api.services.SnippetService
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

@RestController
@RequestMapping("/api/snippets")
class SnippetController(
    private val service: SnippetService,
) {

    @GetMapping
    fun getAll(): ResponseEntity<List<SnippetDTO>> = ResponseEntity.ok(service.getAll())

    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: Long,
    ): ResponseEntity<SnippetDTO> {
        val snippet = service.getById(id)
        return if (snippet != null) {
            ResponseEntity.ok(snippet)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    fun create(
        @RequestBody request: CreateSnippetRequest,
    ): ResponseEntity<SnippetDTO> {
        val snippet = service.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(snippet)
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: UpdateSnippetRequest,
    ): ResponseEntity<SnippetDTO> {
        val snippet = service.update(id, request)
        return if (snippet != null) {
            ResponseEntity.ok(snippet)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        val deleted = service.delete(id)
        return if (deleted) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
