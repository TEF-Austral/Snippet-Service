package controllers

import AsyncTaskRequestContext
import authorization.AuthorizationService
import authorization.Permissions
import common.dtos.requests.TestExecutionRequestDTO
import common.dtos.responses.TestExecutionResponseDTO
import component.PrintScriptServiceClient
import controllers.utils.getSnippet
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
    private val printScriptServiceClient: PrintScriptServiceClient,
    private val snippetRepository: SnippetRepository,
    private val authorizationServiceClient: AuthorizationService,
    private val authenticatedUserProvider: AuthenticatedUserProviderInt,
    private val asyncTaskProducer: AsyncTaskProducerInt,
) {

    @PostMapping("/execute")
    fun executeTest(
        @RequestBody request: TestExecutionRequestDTO,
    ): ResponseEntity<TestExecutionResponseDTO> {
        val userId = authenticatedUserProvider.getCurrentUserId()

        val snippet =
            getSnippet(
                request.snippetId,
                userId,
                Permissions.EDIT,
                snippetRepository,
                authorizationServiceClient,
            )

        val result =
            printScriptServiceClient.executeTest(
                container = snippet.bucketContainer,
                key =
                    snippet.bucketKey
                        ?: throw IllegalStateException("Snippet has no bucket key"),
                version = request.version,
                testId = request.testId,
            )

        return ResponseEntity.ok(result)
    }

    @PostMapping("/execute/async")
    fun executeTestAsync(
        @RequestParam("snippetId") snippetId: Long,
        @RequestParam("version") version: String,
    ): ResponseEntity<Map<String, String>> {
        val userId = authenticatedUserProvider.getCurrentUserId()

        val snippet =
            getSnippet(
                snippetId,
                userId,
                Permissions.EDIT,
                snippetRepository,
                authorizationServiceClient,
            )

        val context =
            AsyncTaskRequestContext(
                snippetId,
                snippet.bucketContainer,
                snippet.bucketKey!!,
                version,
            )
        val requestId =
            asyncTaskProducer.request(
                TaskType.TESTING,
                context,
            )

        return ResponseEntity.accepted().body(
            mapOf(
                "requestId" to requestId,
                "message" to "Test execution request accepted. Processing asynchronously.",
            ),
        )
    }
}
