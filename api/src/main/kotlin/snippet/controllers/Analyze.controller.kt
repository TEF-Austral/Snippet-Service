package snippet.controllers

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import snippet.component.AuthorizationServiceClient
import snippet.component.PrintScriptServiceClient
import snippet.dtos.responses.ValidationResponseDTO
import snippet.producers.AsyncTaskProducer
import snippet.repositories.SnippetRepository
import snippet.security.AuthenticatedUserProvider

@RestController
@RequestMapping("/analyze")
class AnalyzeController(
    private val printScriptServiceClient: PrintScriptServiceClient,
    private val snippetRepository: SnippetRepository,
    private val authorizationServiceClient: AuthorizationServiceClient,
    private val authenticatedUserProvider: AuthenticatedUserProvider,
    private val asyncTaskProducer: AsyncTaskProducer,
) {

    private fun normalizeVersion(version: String): String =
        when (version) {
            "1.1.0" -> "1.1"
            "1.0.0" -> "1.0"
            else -> version
        }

    @GetMapping
    fun analyzeSnippet(
        @RequestParam("snippetId") snippetId: Long,
        @RequestParam("version") version: String,
    ): ResponseEntity<ValidationResponseDTO> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        val normalizedVersion = normalizeVersion(version)

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
            printScriptServiceClient.analyzeSnippet(
                container = snippet.bucketContainer,
                key =
                    snippet.bucketKey
                        ?: throw IllegalStateException("Snippet has no bucket key"),
                version = normalizedVersion,
                userId = userId,
            )

        return ResponseEntity.ok(result)
    }

    @GetMapping("/compile")
    fun compileSnippet(
        @RequestParam("snippetId") snippetId: Long,
        @RequestParam("version") version: String,
    ): ResponseEntity<ValidationResponseDTO> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        val normalizedVersion = normalizeVersion(version)

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
                version = normalizedVersion,
            )

        return ResponseEntity.ok(result)
    }

    @PostMapping("/async")
    fun analyzeSnippetAsync(
        @RequestParam("snippetId") snippetId: Long,
        @RequestParam("version") version: String,
    ): ResponseEntity<Map<String, String>> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        val normalizedVersion = normalizeVersion(version)

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

        val requestId =
            asyncTaskProducer.requestLinting(
                snippetId = snippetId,
                bucketContainer = snippet.bucketContainer,
                bucketKey = snippet.bucketKey!!,
                version = normalizedVersion,
                userId = userId,
                languageId = snippet.id?.toString() ?: "",
            )

        return ResponseEntity.accepted().body(
            mapOf(
                "requestId" to requestId,
                "message" to "Linting request accepted. Processing asynchronously.",
            ),
        )
    }
}
