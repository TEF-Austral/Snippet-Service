package controllers

import AsyncTaskRequestContext
import authorization.AuthorizationService
import authorization.UserAction
import controllers.utils.getSnippet
import dtos.responses.ValidationResponseDTO
import language.LanguageServiceClientInt
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
@RequestMapping("/analyze")
class AnalyzeController(
    private val executionServiceClient: LanguageServiceClientInt,
    private val snippetRepository: SnippetRepository,
    private val authorizationServiceClient: AuthorizationService,
    private val authenticatedUserProvider: AuthenticatedUserProviderInt,
    private val asyncTaskProducer: AsyncTaskProducerInt,
) {

    private val log = org.slf4j.LoggerFactory.getLogger(AnalyzeController::class.java)

    @GetMapping
    fun analyzeSnippet(
        @RequestParam("snippetId") snippetId: Long,
        @RequestParam("version") version: String,
    ): ResponseEntity<ValidationResponseDTO> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        log.info("Analyzing snippet: snippetId=$snippetId, version=$version, userId=$userId")

        val snippet =
            getSnippet(
                snippetId,
                userId,
                UserAction.READ,
                snippetRepository,
                authorizationServiceClient,
            )

        val language = snippet.language.name

        val result =
            executionServiceClient.analyzeSnippet(
                container = snippet.bucketContainer,
                key =
                    snippet.bucketKey
                        ?: throw IllegalStateException("Snippet has no bucket key"),
                version = version,
                userId = userId,
                language = language,
            )

        log.info("Snippet analysis completed: snippetId=$snippetId, success=${result.isValid}")
        return ResponseEntity.ok(result)
    }

    @GetMapping("/club")
    fun getClub(): ResponseEntity<String> {
        log.error("Hola como estas 🦧")
        return ResponseEntity.status(500).body("Internal server error")
    }

    @GetMapping("/compile")
    fun compileSnippet(
        @RequestParam("snippetId") snippetId: Long,
        @RequestParam("version") version: String,
    ): ResponseEntity<ValidationResponseDTO> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        log.info("Compiling snippet: snippetId=$snippetId, version=$version, userId=$userId")

        val snippet =
            getSnippet(
                snippetId,
                userId,
                UserAction.READ,
                snippetRepository,
                authorizationServiceClient,
            )

        val language = snippet.language.name

        val result =
            executionServiceClient.compileSnippet(
                container = snippet.bucketContainer,
                key =
                    snippet.bucketKey
                        ?: throw IllegalStateException("Snippet has no bucket key"),
                version = version,
                language = language,
            )

        log.info("Snippet compilation completed: snippetId=$snippetId, success=${result.isValid}")
        return ResponseEntity.ok(result)
    }

    @PostMapping("/async")
    fun analyzeSnippetAsync(
        @RequestParam("snippetId") snippetId: Long,
        @RequestParam("version") version: String,
    ): ResponseEntity<Map<String, String>> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        log.info(
            "Async linting request received: snippetId=$snippetId, version=$version, userId=$userId",
        )

        try {
            val snippet =
                getSnippet(
                    snippetId,
                    userId,
                    UserAction.READ,
                    snippetRepository,
                    authorizationServiceClient,
                )

            if (snippet.bucketKey == null) {
                log.warn(
                    "Snippet has no bucket key for async linting: snippetId=$snippetId, userId=$userId",
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
                    TaskType.LINTING,
                    context,
                )

            log.info("Async linting request accepted: snippetId=$snippetId, requestId=$requestId")
            return ResponseEntity.accepted().body(
                mapOf(
                    "requestId" to requestId,
                    "message" to "Linting request accepted. Processing asynchronously.",
                ),
            )
        } catch (e: NoSuchElementException) {
            log.warn("Snippet not found for async linting: snippetId=$snippetId, userId=$userId")
            throw e
        } catch (e: IllegalAccessException) {
            log.warn(
                "User does not have permission for async linting: snippetId=$snippetId, userId=$userId",
            )
            throw e
        }
    }
}
