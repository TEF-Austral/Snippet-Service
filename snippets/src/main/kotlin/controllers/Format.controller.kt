package controllers

import AsyncTaskRequestContext
import authorization.AuthorizationService
import authorization.UserAction
import controllers.utils.getSnippet
import language.LanguageServiceClientInt
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
    private val executionServiceClient: LanguageServiceClientInt,
    private val snippetRepository: SnippetRepository,
    private val authorizationServiceClient: AuthorizationService,
    private val authenticatedUserProvider: AuthenticatedUserProviderInt,
    private val asyncTaskProducer: AsyncTaskProducerInt,
) {
    private val log = org.slf4j.LoggerFactory.getLogger(FormatController::class.java)

    @PostMapping
    fun formatSnippet(
        @RequestParam("snippetId") snippetId: Long,
        @RequestParam("version") version: String,
    ): ResponseEntity<String> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        log.info("Formatting snippet: snippetId=$snippetId, version=$version, userId=$userId")

        val snippet =
            getSnippet(
                snippetId,
                userId,
                UserAction.EDIT,
                snippetRepository,
                authorizationServiceClient,
            )

        if (snippet.bucketKey == null) {
            log.warn("Snippet has no bucket key: snippetId=$snippetId, userId=$userId")
            throw IllegalStateException("Snippet has no bucket key")
        }

        val bucketKey = snippet.bucketKey!!

        val formattedContent =
            executionServiceClient.formatSnippet(
                container = snippet.bucketContainer,
                key = bucketKey,
                version = version,
                userId = userId,
                language = snippet.language.name,
            )

        log.info("Snippet formatted successfully: snippetId=$snippetId")
        return ResponseEntity.ok(formattedContent)
    }

    @PostMapping("/preview")
    fun previewFormat(
        @RequestParam("snippetId") snippetId: Long,
        @RequestParam("version") version: String,
    ): ResponseEntity<String> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        log.info(
            "Previewing format for snippet: snippetId=$snippetId, version=$version, userId=$userId",
        )

        val snippet =
            getSnippet(
                snippetId,
                userId,
                UserAction.READ,
                snippetRepository,
                authorizationServiceClient,
            )

        if (snippet.bucketKey == null) {
            log.warn("Snippet has no bucket key for preview: snippetId=$snippetId, userId=$userId")
            throw IllegalStateException("Snippet has no bucket key")
        }

        val bucketKey = snippet.bucketKey!!

        val formattedContent =
            executionServiceClient.previewFormat(
                container = snippet.bucketContainer,
                key = bucketKey,
                version = version,
                userId = userId,
                language = snippet.language.name,
            )

        log.info("Format preview generated successfully: snippetId=$snippetId")
        return ResponseEntity.ok(formattedContent)
    }

    @GetMapping("/download")
    fun downloadFormatted(
        @RequestParam("snippetId") snippetId: Long,
        @RequestParam("version") version: String,
    ): ResponseEntity<Resource> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        log.info(
            "Downloading formatted snippet: snippetId=$snippetId, version=$version, userId=$userId",
        )

        val snippet =
            getSnippet(
                snippetId,
                userId,
                UserAction.READ,
                snippetRepository,
                authorizationServiceClient,
            )

        if (snippet.bucketKey == null) {
            log.warn("Snippet has no bucket key for download: snippetId=$snippetId, userId=$userId")
            throw IllegalStateException("Snippet has no bucket key")
        }

        val bucketKey = snippet.bucketKey!!

        val formattedBytes =
            executionServiceClient.downloadFormatted(
                container = snippet.bucketContainer,
                key = bucketKey,
                version = version,
                language = snippet.language.name,
            )

        val resource = ByteArrayResource(formattedBytes)

        log.info(
            "Formatted snippet downloaded successfully: snippetId=$snippetId, fileName=${snippet.name}-formatted.ps",
        )
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
        log.info(
            "Async formatting request received: snippetId=$snippetId, version=$version, userId=$userId",
        )

        val snippet =
            getSnippet(
                snippetId,
                userId,
                UserAction.EDIT,
                snippetRepository,
                authorizationServiceClient,
            )

        if (snippet.bucketKey == null) {
            log.warn(
                "Snippet has no bucket key for async formatting: snippetId=$snippetId, userId=$userId",
            )
            throw IllegalStateException("Snippet has no bucket key")
        }

        val bucketKey = snippet.bucketKey!!

        val context =
            AsyncTaskRequestContext(
                snippetId = snippetId,
                bucketContainer = snippet.bucketContainer,
                bucketKey = bucketKey,
                version = version,
                userId = userId,
                languageId = snippet.language.name,
            )

        val requestId =
            asyncTaskProducer.request(
                TaskType.FORMATTING,
                context,
            )

        log.info("Async formatting request accepted: snippetId=$snippetId, requestId=$requestId")
        return ResponseEntity.accepted().body(
            mapOf(
                "requestId" to requestId,
                "message" to "Formatting request accepted. Processing asynchronously.",
            ),
        )
    }
}
