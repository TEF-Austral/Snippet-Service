package controllers

import AsyncTaskRequestContext
import authorization.AuthorizationService
import authorization.UserAction
import dtos.responses.ValidationResponseDTO
import controllers.utils.getSnippet
import language.ExecutionServiceClientInt
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import producers.AsyncTaskProducerInt
import producers.strategy.TaskType
import repositories.SnippetRepository
import security.AuthenticatedUserProvider

@RestController
@RequestMapping("/analyze")
class AnalyzeController(
    private val executionServiceClient: ExecutionServiceClientInt,
    private val snippetRepository: SnippetRepository,
    private val authorizationServiceClient: AuthorizationService,
    private val authenticatedUserProvider: AuthenticatedUserProvider,
    private val asyncTaskProducer: AsyncTaskProducerInt,
) {
    @GetMapping
    fun analyzeSnippet(
        @RequestParam("snippetId") snippetId: Long,
        @RequestParam("version") version: String,
    ): ResponseEntity<ValidationResponseDTO> {
        val userId = authenticatedUserProvider.getCurrentUserId()

        val snippet =
            getSnippet(
                snippetId,
                userId,
                UserAction.READ,
                snippetRepository,
                authorizationServiceClient,
            )

        val result =
            executionServiceClient.analyzeSnippet(
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
    ): ResponseEntity<ValidationResponseDTO> {
        val userId = authenticatedUserProvider.getCurrentUserId()

        val snippet =
            getSnippet(
                snippetId,
                userId,
                UserAction.READ,
                snippetRepository,
                authorizationServiceClient,
            )

        val result =
            executionServiceClient.compileSnippet(
                container = snippet.bucketContainer,
                key =
                    snippet.bucketKey
                        ?: throw IllegalStateException("Snippet has no bucket key"),
                version = version,
            )

        return ResponseEntity.ok(result)
    }

    @PostMapping("/async")
    fun analyzeSnippetAsync(
        @RequestParam("snippetId") snippetId: Long,
        @RequestParam("version") version: String,
    ): ResponseEntity<Map<String, String>> {
        val userId = authenticatedUserProvider.getCurrentUserId()

        val snippet =
            getSnippet(
                snippetId,
                userId,
                UserAction.READ,
                snippetRepository,
                authorizationServiceClient,
            )

        val context =
            AsyncTaskRequestContext(
                snippetId = snippetId,
                bucketContainer = snippet.bucketContainer,
                bucketKey = snippet.bucketKey!!,
                version = version,
                userId = userId,
                languageId = snippet.language.name,
            )

        val requestId =
            asyncTaskProducer.request(
                TaskType.LINTING,
                context,
            )

        return ResponseEntity.accepted().body(
            mapOf(
                "requestId" to requestId,
                "message" to "Linting request accepted. Processing asynchronously.",
            ),
        )
    }
}
