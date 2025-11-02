package snippet.controllers

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import snippet.component.AuthorizationServiceClient
import snippet.dtos.PermissionsSummary
import snippet.dtos.ShareSnippetDTO
import snippet.dtos.ShareSnippetResponseDTO
import snippet.repositories.SnippetRepository
import snippet.security.AuthenticatedUserProvider

@RestController
@RequestMapping("/snippets")
class SnippetSharingController(
    private val snippetRepository: SnippetRepository,
    private val authorizationServiceClient: AuthorizationServiceClient,
    private val authenticatedUserProvider: AuthenticatedUserProvider,
) {

    @PostMapping("/{id}/share")
    fun shareSnippet(
        @PathVariable id: Long,
        @Valid @RequestBody requestDTO: ShareSnippetDTO,
    ): ResponseEntity<ShareSnippetResponseDTO> {
        val userId = authenticatedUserProvider.getCurrentUserId()

        val snippet =
            snippetRepository
                .findById(id)
                .orElseThrow { NoSuchElementException("Snippet not found: $id") }

        val hasPermission =
            authorizationServiceClient.checkPermission(
                userId = userId,
                action = "share",
                snippetId = id.toString(),
                ownerId = snippet.ownerId,
            )

        if (!hasPermission) {
            throw IllegalAccessException("You don't have permission to share this snippet")
        }

        grantPermissionToUser(
            requesterId = userId,
            ownerId = snippet.ownerId,
            granteeId = requestDTO.userId,
            snippetId = id.toString(),
            canRead = requestDTO.canRead,
            canEdit = requestDTO.canEdit,
        )

        return ResponseEntity.ok(
            ShareSnippetResponseDTO(
                message = "Snippet shared successfully",
                snippetId = id,
                sharedWith = requestDTO.userId,
                permissions =
                    PermissionsSummary(
                        canRead = requestDTO.canRead,
                        canEdit = requestDTO.canEdit,
                    ),
            ),
        )
    }

    @DeleteMapping("/{id}/share/{userId}")
    fun revokeAccess(
        @PathVariable id: Long,
        @PathVariable userId: String,
    ): ResponseEntity<Unit> {
        val requesterId = authenticatedUserProvider.getCurrentUserId()

        val snippet =
            snippetRepository
                .findById(id)
                .orElseThrow { NoSuchElementException("Snippet not found: $id") }

        val hasPermission =
            authorizationServiceClient.checkPermission(
                userId = requesterId,
                action = "share",
                snippetId = id.toString(),
                ownerId = snippet.ownerId,
            )

        if (!hasPermission) {
            throw IllegalAccessException("You don't have permission to revoke access")
        }

        revokePermissionFromUser(
            requesterId = requesterId,
            userId = userId,
            snippetId = id.toString(),
        )

        return ResponseEntity.noContent().build()
    }

    private fun grantPermissionToUser(
        requesterId: String,
        ownerId: String,
        granteeId: String,
        snippetId: String,
        canRead: Boolean,
        canEdit: Boolean,
    ): Boolean {
        authorizationServiceClient.grantPermission(
            requesterId = requesterId,
            ownerId = ownerId,
            granteeId = granteeId,
            snippetId = snippetId,
            canRead = canRead,
            canEdit = canEdit,
        )
        return true
    }

    private fun revokePermissionFromUser(
        requesterId: String,
        userId: String,
        snippetId: String,
    ) {
        authorizationServiceClient.revokePermission(
            requesterId = requesterId,
            userId = userId,
            snippetId = snippetId,
        )
    }
}
