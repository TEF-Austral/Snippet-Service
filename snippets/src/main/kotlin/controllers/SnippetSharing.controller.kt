package controllers

import authorization.AuthorizationService
import authorization.UserAction
import dtos.requests.ShareSnippetDTO
import dtos.responses.PermissionsSummaryDTO
import dtos.responses.ShareSnippetResponseDTO
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import repositories.SnippetRepository
import security.AuthenticatedUserProviderInt

@RestController
@RequestMapping("/snippets")
class SnippetSharingController(
    private val snippetRepository: SnippetRepository,
    private val authorizationServiceClient: AuthorizationService,
    private val authenticatedUserProvider: AuthenticatedUserProviderInt,
) {
    private val log = org.slf4j.LoggerFactory.getLogger(SnippetSharingController::class.java)

    @PostMapping("/{id}/share")
    fun shareSnippet(
        @PathVariable id: Long,
        @Valid @RequestBody requestDTO: ShareSnippetDTO,
    ): ResponseEntity<ShareSnippetResponseDTO> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        log.info("POST /snippets/$id/share - Sharing snippet with user ${requestDTO.userId}")

        val snippet =
            snippetRepository
                .findById(id)
                .orElseThrow { NoSuchElementException("Snippet not found: $id") }

        val hasPermission =
            authorizationServiceClient.checkPermission(
                userId = userId,
                action = UserAction.SHARE,
                snippetId = id.toString(),
                ownerId = snippet.ownerId,
            )

        if (!hasPermission) {
            log.warn("POST /snippets/$id/share - Permission denied for user $userId")
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

        log.warn(
            "POST /snippets/$id/share - Snippet shared successfully with user ${requestDTO.userId}",
        )
        return ResponseEntity.ok(
            ShareSnippetResponseDTO(
                message = "Snippet shared successfully",
                snippetId = id,
                sharedWith = requestDTO.userId,
                permissions =
                    PermissionsSummaryDTO(
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
        log.info("DELETE /snippets/$id/share/$userId - Revoking access")

        val snippet =
            snippetRepository
                .findById(id)
                .orElseThrow { NoSuchElementException("Snippet not found: $id") }

        val hasPermission =
            authorizationServiceClient.checkPermission(
                userId = requesterId,
                action = UserAction.SHARE,
                snippetId = id.toString(),
                ownerId = snippet.ownerId,
            )

        if (!hasPermission) {
            log.warn(
                "DELETE /snippets/$id/share/$userId - Permission denied for requester $requesterId",
            )
            throw IllegalAccessException("You don't have permission to revoke access")
        }

        revokePermissionFromUser(
            requesterId = requesterId,
            userId = userId,
            snippetId = id.toString(),
        )

        log.warn("DELETE /snippets/$id/share/$userId - Access revoked successfully")
        return ResponseEntity.noContent().build()
    }

    private fun grantPermissionToUser(
        requesterId: String,
        ownerId: String,
        granteeId: String,
        snippetId: String,
        canRead: Boolean,
        canEdit: Boolean,
    ) {
        authorizationServiceClient.grantPermission(
            requesterId = requesterId,
            ownerId = ownerId,
            granteeId = granteeId,
            snippetId = snippetId,
            canRead = canRead,
            canEdit = canEdit,
        )
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
