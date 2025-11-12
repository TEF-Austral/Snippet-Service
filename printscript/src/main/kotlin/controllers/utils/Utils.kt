package controllers.utils

import authorization.AuthorizationService
import authorization.Permissions
import entity.Snippet
import repositories.SnippetRepository

fun getSnippet(
    snippetId: Long,
    userId: String,
    permissions: Permissions,
    snippetRepository: SnippetRepository,
    authorizationServiceClient: AuthorizationService,
): Snippet {
    val snippet =
        snippetRepository
            .findById(snippetId)
            .orElseThrow { NoSuchElementException("Snippet not found: $snippetId") }

    val hasPermission =
        authorizationServiceClient.checkPermission(
            userId = userId,
            action = permissions,
            snippetId = snippetId.toString(),
            ownerId = snippet.ownerId,
        )

    if (!hasPermission) {
        throw IllegalAccessException("You don't have permission to analyze this snippet")
    }

    return snippet
}
