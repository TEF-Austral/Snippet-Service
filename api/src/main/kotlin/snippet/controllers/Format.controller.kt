package snippet.controllers

import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import snippet.component.AuthorizationServiceClient
import snippet.component.PrintScriptServiceClient
import snippet.producers.AsyncTaskProducer
import snippet.dtos.FormatConfigDTO
import snippet.repositories.SnippetRepository
import snippet.security.AuthenticatedUserProvider

@RestController
@RequestMapping("/format")
class FormatController(
    private val printScriptServiceClient: PrintScriptServiceClient,
    private val snippetRepository: SnippetRepository,
    private val authorizationServiceClient: AuthorizationServiceClient,
    private val authenticatedUserProvider: AuthenticatedUserProvider,
    private val asyncTaskProducer: AsyncTaskProducer,
) {

    @PostMapping
    fun formatSnippet(
        @RequestParam("snippetId") snippetId: Long,
        @RequestParam("version") version: String,
        @RequestBody config: FormatConfigDTO,
    ): ResponseEntity<String> {
        val userId = authenticatedUserProvider.getCurrentUserId()

        val snippet =
            snippetRepository
                .findById(snippetId)
                .orElseThrow { NoSuchElementException("Snippet not found: $snippetId") }

        val hasPermission =
            authorizationServiceClient.checkPermission(
                userId = userId,
                action = "edit",
                snippetId = snippetId.toString(),
                ownerId = snippet.ownerId,
            )

        if (!hasPermission) {
            throw IllegalAccessException("You don't have permission to format this snippet")
        }

        val formattedContent =
            printScriptServiceClient.formatSnippet(
                container = snippet.bucketContainer,
                key =
                    snippet.bucketKey
                        ?: throw IllegalStateException("Snippet has no bucket key"),
                version = version,
                config = config,
            )

        return ResponseEntity.ok(formattedContent)
    }

    @PostMapping("/preview")
    fun previewFormat(
        @RequestParam("snippetId") snippetId: Long,
        @RequestParam("version") version: String,
        @RequestBody config: FormatConfigDTO,
    ): ResponseEntity<String> {
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
            throw IllegalAccessException("You don't have permission to access this snippet")
        }

        val formattedContent =
            printScriptServiceClient.previewFormat(
                container = snippet.bucketContainer,
                key =
                    snippet.bucketKey
                        ?: throw IllegalStateException("Snippet has no bucket key"),
                version = version,
                config = config,
            )

        return ResponseEntity.ok(formattedContent)
    }

    @GetMapping("/download")
    fun downloadFormatted(
        @RequestParam("snippetId") snippetId: Long,
        @RequestParam("version") version: String,
        @RequestBody config: FormatConfigDTO,
    ): ResponseEntity<Resource> {
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
            throw IllegalAccessException("You don't have permission to access this snippet")
        }

        val formattedBytes =
            printScriptServiceClient.downloadFormatted(
                container = snippet.bucketContainer,
                key =
                    snippet.bucketKey
                        ?: throw IllegalStateException("Snippet has no bucket key"),
                version = version,
                config = config,
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

        val snippet = snippetRepository.findById(snippetId)
            .orElseThrow { NoSuchElementException("Snippet not found: $snippetId") }

        val hasPermission = authorizationServiceClient.checkPermission(
            userId = userId,
            action = "edit",
            snippetId = snippetId.toString(),
            ownerId = snippet.ownerId
        )

        if (!hasPermission) {
            throw IllegalAccessException("You don't have permission to format this snippet")
        }

        val requestId = asyncTaskProducer.requestFormatting(
            snippetId = snippetId,
            bucketContainer = snippet.bucketContainer,
            bucketKey = snippet.bucketKey!!,
            version = version,
            userId = userId
        )

        return ResponseEntity.accepted().body(
            mapOf(
                "requestId" to requestId,
                "message" to "Formatting request accepted. Processing asynchronously."
            )
        )
    }
}
