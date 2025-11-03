package snippet.controllers

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import snippet.component.AuthorizationServiceClient
import snippet.component.PrintScriptServiceClient
import snippet.component.TestExecutionResponseDTO
import snippet.repositories.SnippetRepository
import snippet.security.AuthenticatedUserProvider

@RestController
@RequestMapping("/tests")
class TestController(
    private val printScriptServiceClient: PrintScriptServiceClient,
    private val snippetRepository: SnippetRepository,
    private val authorizationServiceClient: AuthorizationServiceClient,
    private val authenticatedUserProvider: AuthenticatedUserProvider,
) {

    @PostMapping("/execute")
    fun executeTest(
        @RequestParam("snippetId") snippetId: Long,
        @RequestParam("version") version: String,
        @RequestParam("testId") testId: Long,
    ): ResponseEntity<TestExecutionResponseDTO> {
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
            throw IllegalAccessException("You don't have permission to execute tests on this snippet")
        }

        val result =
            printScriptServiceClient.executeTest(
                container = snippet.bucketContainer,
                key = snippet.bucketKey
                    ?: throw IllegalStateException("Snippet has no bucket key"),
                version = version,
                testId = testId,
            )

        return ResponseEntity.ok(result)
    }
}