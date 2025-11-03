package snippet.controllers

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import snippet.component.AuthorizationServiceClient
import snippet.component.PrintScriptServiceClient
import snippet.component.ValidationResponse
import snippet.repositories.SnippetRepository
import snippet.security.AuthenticatedUserProvider

@RestController
@RequestMapping("/analyze")
class AnalyzeController(
    private val printScriptServiceClient: PrintScriptServiceClient,
    private val snippetRepository: SnippetRepository,
    private val authorizationServiceClient: AuthorizationServiceClient,
    private val authenticatedUserProvider: AuthenticatedUserProvider,
) {

    @GetMapping
    fun analyzeSnippet(
        @RequestParam("snippetId") snippetId: Long,
        @RequestParam("version") version: String,
    ): ResponseEntity<ValidationResponse> {
        val userId = authenticatedUserProvider.getCurrentUserId()

        val snippet =
            snippetRepository
                .findById(snippetId)
                .orElseThrow { NoSuchElementException("Snippet not found: $snippetId") }

        val hasPermission =
            authorizationServiceClient.checkPermission(
                userId = userId,
                action = "read",
                snippetId = snippetId.toString(),
                ownerId = snippet.ownerId,
            )

        if (!hasPermission) {
            throw IllegalAccessException("You don't have permission to analyze this snippet")
        }

        val result =
            printScriptServiceClient.validateSnippet(
                container = snippet.bucketContainer,
                key =
                    snippet.bucketKey
                        ?: throw IllegalStateException("Snippet has no bucket key"),
                version = version,
                userId = userId,
            )

        return ResponseEntity.ok(result)
    }

    @GetMapping("/compile")
    fun compileSnippet(
        @RequestParam("snippetId") snippetId: Long,
        @RequestParam("version") version: String,
    ): ResponseEntity<ValidationResponse> {
        val userId = authenticatedUserProvider.getCurrentUserId()

        val snippet =
            snippetRepository
                .findById(snippetId)
                .orElseThrow { NoSuchElementException("Snippet not found: $snippetId") }

        val hasPermission =
            authorizationServiceClient.checkPermission(
                userId = userId,
                action = "read",
                snippetId = snippetId.toString(),
                ownerId = snippet.ownerId,
            )

        if (!hasPermission) {
            throw IllegalAccessException("You don't have permission to compile this snippet")
        }

        val result =
            printScriptServiceClient.compileSnippet(
                container = snippet.bucketContainer,
                key =
                    snippet.bucketKey
                        ?: throw IllegalStateException("Snippet has no bucket key"),
                version = version,
            )

        return ResponseEntity.ok(result)
    }
}
