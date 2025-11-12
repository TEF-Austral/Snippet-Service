package controllers

import AsyncTaskRequestContext
import authorization.AuthorizationServiceClient
import authorization.UserAction
import controllers.utils.getSnippet
import language.ExecutionServiceClientInt
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import producers.AsyncTaskProducerInt
import producers.strategy.TaskType
import repositories.SnippetRepository
import security.AuthenticatedUserProviderInt

@RestController
@RequestMapping("/format")
class FormatController(
    private val executionServiceClient: ExecutionServiceClientInt,
    private val snippetRepository: SnippetRepository,
    private val authorizationServiceClient: AuthorizationServiceClient,
    private val authenticatedUserProvider: AuthenticatedUserProviderInt,
    private val asyncTaskProducer: AsyncTaskProducerInt,
) {
    @PostMapping
    fun formatSnippet(
        @RequestParam("snippetId") snippetId: Long,
        @RequestParam("version") version: String,
    ): ResponseEntity<String> {
        val userId = authenticatedUserProvider.getCurrentUserId()

        val snippet =
            getSnippet(
                snippetId,
                userId,
                UserAction.EDIT,
                snippetRepository,
                authorizationServiceClient,
            )

        val formattedContent =
            executionServiceClient.formatSnippet(
                container = snippet.bucketContainer,
                key =
                    snippet.bucketKey
                        ?: throw IllegalStateException("Snippet has no bucket key"),
                version = version,
                userId = userId,
            )

        return ResponseEntity.ok(formattedContent)
    }

    @PostMapping("/preview")
    fun previewFormat(
        @RequestParam("snippetId") snippetId: Long,
        @RequestParam("version") version: String,
    ): ResponseEntity<String> {
        val userId = authenticatedUserProvider.getCurrentUserId()

        val snippet =
            getSnippet(
                snippetId,
                userId,
                UserAction.READ,
                snippetRepository,
                authorizationServiceClient,
            )

        val formattedContent =
            executionServiceClient.previewFormat(
                container = snippet.bucketContainer,
                key =
                    snippet.bucketKey
                        ?: throw IllegalStateException("Snippet has no bucket key"),
                version = version,
                userId = userId,
            )

        return ResponseEntity.ok(formattedContent)
    }

    @GetMapping("/download")
    fun downloadFormatted(
        @RequestParam("snippetId") snippetId: Long,
        @RequestParam("version") version: String,
    ): ResponseEntity<Resource> {
        val userId = authenticatedUserProvider.getCurrentUserId()

        val snippet =
            getSnippet(
                snippetId,
                userId,
                UserAction.READ,
                snippetRepository,
                authorizationServiceClient,
            )

        val formattedBytes =
            executionServiceClient.downloadFormatted(
                container = snippet.bucketContainer,
                key =
                    snippet.bucketKey
                        ?: throw IllegalStateException("Snippet has no bucket key"),
                version = version,
            )

        val resource = ByteArrayResource(formattedBytes)

        return ResponseEntity
            .ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"${snippet.name}-formatted.ps\"",
            ).contentType(MediaType.TEXT_PLAIN)
            .body(resource)
    }

    @PostMapping("/async")
    fun formatSnippetAsync(
        @RequestParam("snippetId") snippetId: Long,
        @RequestParam("version") version: String,
    ): ResponseEntity<Map<String, String>> {
        val userId = authenticatedUserProvider.getCurrentUserId()

        val snippet =
            getSnippet(
                snippetId,
                userId,
                UserAction.EDIT,
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
                TaskType.FORMATTING,
                context,
            )

        return ResponseEntity.accepted().body(
            mapOf(
                "requestId" to requestId,
                "message" to "Formatting request accepted. Processing asynchronously.",
            ),
        )
    }
}
