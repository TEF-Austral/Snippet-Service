package controllers

import common.Language
import common.dtos.requests.ComplianceFilter
import common.dtos.requests.CreateSnippetRequestDTO
import common.dtos.requests.OwnershipFilter
import common.dtos.requests.SnippetFilterDTO
import common.dtos.requests.SortField
import common.dtos.requests.SortOrder
import common.dtos.requests.UpdateSnippetRequestDTO
import common.dtos.responses.PaginatedSnippetsDTO
import common.dtos.responses.SnippetResponseDTO
import common.dtos.responses.StreamSnippetResponseDTO
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
import common.services.SnippetService
import security.AuthenticatedUserProviderInt

@RestController
@RequestMapping("/snippets")
class SnippetController(
    private val service: SnippetService,
    private val authenticatedUserProvider: AuthenticatedUserProviderInt,
) {

    @PostMapping("")
    fun createSnippet(
        @Valid @RequestBody requestDTO: CreateSnippetRequestDTO,
    ): ResponseEntity<SnippetResponseDTO> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        val author = authenticatedUserProvider.getCurrentUserName() ?: "Unknown"
        val created = service.createSnippet(requestDTO, userId, author)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @DeleteMapping("/{id}")
    fun deleteSnippet(
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        service.deleteSnippet(id, userId)
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/{id}")
    fun updateSnippet(
        @PathVariable id: Long,
        @Valid @RequestBody requestDTO: UpdateSnippetRequestDTO,
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
        return ResponseEntity.ok(result)
    }

    @GetMapping("/MySnippetsThatHavePermission")
    fun getSnippetsThatUserHavePermission(): ResponseEntity<List<StreamSnippetResponseDTO>> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        val result = service.getSnippetsThatUserHavePermission(userId)
        return ResponseEntity.ok(result)
    }
}
