package controllers

import AsyncTaskRequestContext
import authorization.AuthorizationService
import authorization.UserAction
import controllers.utils.getSnippet
import dtos.requests.TestExecutionRequestDTO
import dtos.responses.TestExecutionResponseDTO
import language.LanguageServiceClientInt
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import producers.AsyncTaskProducerInt
import producers.strategy.TaskType
import repositories.SnippetRepository
import security.AuthenticatedUserProviderInt

@RestController
@RequestMapping("/tests")
class TestController(
    private val executionServiceClient: LanguageServiceClientInt,
    private val snippetRepository: SnippetRepository,
    private val authorizationServiceClient: AuthorizationService,
    private val authenticatedUserProvider: AuthenticatedUserProviderInt,
    private val asyncTaskProducer: AsyncTaskProducerInt,
) {
    private val log = org.slf4j.LoggerFactory.getLogger(TestController::class.java)

    @PostMapping("/execute")
    fun executeTest(
        @RequestBody request: TestExecutionRequestDTO,
    ): ResponseEntity<TestExecutionResponseDTO> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        log.info(
            "Executing test: snippetId=${request.snippetId}, version=${request.version}, testId=${request.testId}, userId=$userId",
        )

        val snippet =
            getSnippet(
                request.snippetId,
                userId,
                UserAction.EDIT,
                snippetRepository,
                authorizationServiceClient,
            )

        if (snippet.bucketKey == null) {
            log.warn("Snippet has no bucket key: snippetId=${request.snippetId}, userId=$userId")
            throw IllegalStateException("Snippet has no bucket key")
        }

        val bucketKey = snippet.bucketKey!!

        val result =
            executionServiceClient.executeTest(
                container = snippet.bucketContainer,
                key = bucketKey,
                version = request.version,
                testId = request.testId,
                language = snippet.language.name,
            )

        log.info(
            "Test execution completed: snippetId=${request.snippetId}, testId=${request.testId}, passed=${result.passed}",
        )
        return ResponseEntity.ok(result)
    }

    @PostMapping("/execute/async")
    fun executeTestAsync(
        @RequestParam("snippetId") snippetId: Long,
        @RequestParam("version") version: String,
    ): ResponseEntity<Map<String, String>> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        log.info(
            "Async test execution request received: snippetId=$snippetId, version=$version, userId=$userId",
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
                "Snippet has no bucket key for async test execution: snippetId=$snippetId, userId=$userId",
            )
            throw IllegalStateException("Snippet has no bucket key")
        }

        val bucketKey = snippet.bucketKey!!

        val context =
            AsyncTaskRequestContext(
                snippetId,
                snippet.bucketContainer,
                bucketKey,
                version,
            )
        val requestId =
            asyncTaskProducer.request(
                TaskType.TESTING,
                context,
            )

        log.info(
            "Async test execution request accepted: snippetId=$snippetId, requestId=$requestId",
        )
        return ResponseEntity.accepted().body(
            mapOf(
                "requestId" to requestId,
                "message" to "Test execution request accepted. Processing asynchronously.",
            ),
        )
    }
}
