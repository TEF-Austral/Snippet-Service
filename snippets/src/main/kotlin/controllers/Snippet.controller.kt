package controllers

import dtos.types.ComplianceFilter
import dtos.requests.CreateSnippetRequestDTO
import dtos.types.OwnershipFilter
import dtos.requests.SnippetFilterDTO
import dtos.types.SortField
import dtos.types.SortOrder
import dtos.requests.UpdateSnippetRequestDTO
import dtos.responses.PaginatedSnippetsDTO
import dtos.responses.SnippetResponseDTO
import dtos.responses.StreamSnippetResponseDTO
import dtos.types.Language
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import security.AuthenticatedUserProviderInt
import services.SnippetService

@RestController
@RequestMapping("/snippets")
class SnippetController(
    private val service: SnippetService,
    private val authenticatedUserProvider: AuthenticatedUserProviderInt,
) {
    private val log = org.slf4j.LoggerFactory.getLogger(SnippetController::class.java)

    @PostMapping("")
    fun createSnippet(
        @Valid @RequestBody requestDTO: CreateSnippetRequestDTO,
    ): ResponseEntity<SnippetResponseDTO> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        val author = authenticatedUserProvider.getCurrentUserName() ?: "Unknown"
        log.info("POST /snippets - Creating snippet for user $userId")
        val created = service.createSnippet(requestDTO, userId, author)
        log.warn("POST /snippets - Snippet created successfully")
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @DeleteMapping("/{id}")
    fun deleteSnippet(
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        log.info("DELETE /snippets/$id - Deleting snippet for user $userId")
        service.deleteSnippet(id, userId)
        log.warn("DELETE /snippets/$id - Snippet deleted successfully")
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/{id}")
    fun updateSnippet(
        @PathVariable id: Long,
        @Valid @RequestBody requestDTO: UpdateSnippetRequestDTO,
    ): ResponseEntity<SnippetResponseDTO> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        log.info("PUT /snippets/$id - Updating snippet for user $userId")
        val updated = service.updateSnippet(id, requestDTO, userId)
        log.warn("PUT /snippets/$id - Snippet updated successfully")
        return ResponseEntity.ok(updated)
    }

    @GetMapping("/{id}")
    fun getSnippet(
        @PathVariable id: Long,
    ): ResponseEntity<SnippetResponseDTO> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        log.info("GET /snippets/$id - Fetching snippet for user $userId")
        val snippet = service.getSnippetById(id, userId)
        log.warn("GET /snippets/$id - Snippet retrieved successfully")
        return ResponseEntity.ok(snippet)
    }

    @GetMapping("")
    fun getMySnippets(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @RequestParam(defaultValue = "ALL") ownership: String,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) language: Language?,
        @RequestParam(defaultValue = "ALL") compliance: String,
        @RequestParam(defaultValue = "NAME") sortBy: String,
        @RequestParam(defaultValue = "ASC") sortOrder: String,
    ): ResponseEntity<PaginatedSnippetsDTO> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        log.info(
            "GET /snippets - Fetching snippets for user $userId, page $page, pageSize $pageSize",
        )

        val filterDTO =
            SnippetFilterDTO(
                ownership =
                    try {
                        OwnershipFilter.valueOf(ownership.uppercase())
                    } catch (e: Exception) {
                        OwnershipFilter.ALL
                    },
                name = name,
                language = language,
                compliance =
                    try {
                        ComplianceFilter.valueOf(compliance.uppercase())
                    } catch (e: Exception) {
                        ComplianceFilter.ALL
                    },
                sortBy =
                    try {
                        SortField.valueOf(sortBy.uppercase())
                    } catch (e: Exception) {
                        SortField.NAME
                    },
                sortOrder =
                    try {
                        SortOrder.valueOf(sortOrder.uppercase())
                    } catch (e: Exception) {
                        SortOrder.ASC
                    },
            )

        val result = service.getMySnippets(userId, page, pageSize, filterDTO)
        log.warn("GET /snippets - Retrieved ${result.snippets.size} snippets")
        return ResponseEntity.ok(result)
    }

    @GetMapping("/MySnippetsThatHavePermission")
    fun getSnippetsThatUserHavePermission(): ResponseEntity<List<StreamSnippetResponseDTO>> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        log.info(
            "GET /snippets/MySnippetsThatHavePermission - Fetching snippets with permissions for user $userId",
        )
        val result = service.getSnippetsThatUserHavePermission(userId)
        log.warn("GET /snippets/MySnippetsThatHavePermission - Retrieved ${result.size} snippets")
        return ResponseEntity.ok(result)
    }
}
