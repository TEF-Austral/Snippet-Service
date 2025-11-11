package snippet.controllers

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import snippet.component.AuthorizationServiceClient
import snippet.component.PrintScriptServiceClient
import snippet.dtos.TestDTO
import snippet.dtos.requests.CreateTestRequestDTO
import snippet.repositories.SnippetRepository
import snippet.security.AuthenticatedUserProvider

@RestController
@RequestMapping("/testcases")
class TestCasesController(
    private val printScriptServiceClient: PrintScriptServiceClient,
    private val authenticatedUserProvider: AuthenticatedUserProvider,
    private val snippetRepository: SnippetRepository,
    private val authorizationServiceClient: AuthorizationServiceClient,
) {
    private val log = org.slf4j.LoggerFactory.getLogger(TestCasesController::class.java)

    @PostMapping
    fun createTest(
        @RequestBody request: CreateTestRequestDTO,
    ): ResponseEntity<TestDTO> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        log.info("POST /testcases - Creating test for snippet ${request.snippetId}, user $userId")

        val snippet =
            snippetRepository
                .findById(request.snippetId)
                .orElseThrow { NoSuchElementException("Snippet not found: ${request.snippetId}") }

        val hasPermission =
            authorizationServiceClient.checkPermission(
                userId = userId,
                action = "edit",
                snippetId = request.snippetId.toString(),
                ownerId = snippet.ownerId,
            )

        if (!hasPermission) {
            log.warn(
                "POST /testcases - Permission denied for user $userId on snippet ${request.snippetId}",
            )
            throw IllegalAccessException(
                "You don't have permission to create tests for this snippet",
            )
        }

        val test = printScriptServiceClient.createTestCase(request)
        log.warn("POST /testcases - Test created with id ${test.id}")
        return ResponseEntity.status(HttpStatus.CREATED).body(test)
    }

    @GetMapping
    fun getTestsBySnippet(
        @RequestParam("snippetId") snippetId: Long,
    ): ResponseEntity<List<TestDTO>> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        log.info("GET /testcases - Fetching tests for snippet $snippetId, user $userId")

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
            log.warn("GET /testcases - Permission denied for user $userId on snippet $snippetId")
            throw IllegalAccessException("You don't have permission to view tests for this snippet")
        }

        val tests = printScriptServiceClient.getTestsBySnippet(snippetId)
        log.warn("GET /testcases - Retrieved ${tests.size} tests for snippet $snippetId")
        return ResponseEntity.ok(tests)
    }

    @GetMapping("/{id}")
    fun getTest(
        @PathVariable id: Long,
    ): ResponseEntity<TestDTO> {
        log.info("GET /testcases/$id - Fetching test")
        val test = printScriptServiceClient.getTestById(id)

        val userId = authenticatedUserProvider.getCurrentUserId()
        val snippet =
            snippetRepository
                .findById(test.snippetId)
                .orElseThrow { NoSuchElementException("Snippet not found: ${test.snippetId}") }

        val hasPermission =
            authorizationServiceClient.checkPermission(
                userId = userId,
                action = "read",
                snippetId = test.snippetId.toString(),
                ownerId = snippet.ownerId,
            )

        if (!hasPermission) {
            log.warn("GET /testcases/$id - Permission denied for user $userId")
            throw IllegalAccessException("You don't have permission to view this test")
        }

        log.warn("GET /testcases/$id - Test retrieved successfully")
        return ResponseEntity.ok(test)
    }

    @DeleteMapping("/{id}")
    fun deleteTest(
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        log.info("DELETE /testcases/$id - Deleting test")
        val test = printScriptServiceClient.getTestById(id)

        val userId = authenticatedUserProvider.getCurrentUserId()
        val snippet =
            snippetRepository
                .findById(test.snippetId)
                .orElseThrow { NoSuchElementException("Snippet not found: ${test.snippetId}") }

        val hasPermission =
            authorizationServiceClient.checkPermission(
                userId = userId,
                action = "edit",
                snippetId = test.snippetId.toString(),
                ownerId = snippet.ownerId,
            )

        if (!hasPermission) {
            log.warn("DELETE /testcases/$id - Permission denied for user $userId")
            throw IllegalAccessException("You don't have permission to delete this test")
        }

        printScriptServiceClient.deleteTestCase(id)
        log.warn("DELETE /testcases/$id - Test deleted successfully")
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/{id}")
    fun updateTest(
        @PathVariable id: Long,
        @RequestBody request: CreateTestRequestDTO,
    ): ResponseEntity<TestDTO> {
        val test = printScriptServiceClient.getTestById(id)
        val userId = authenticatedUserProvider.getCurrentUserId()
        val snippet =
            snippetRepository
                .findById(test.snippetId)
                .orElseThrow { NoSuchElementException("Snippet not found: ${test.snippetId}") }
        val hasPermission =
            authorizationServiceClient.checkPermission(
                userId = userId,
                action = "edit",
                snippetId = test.snippetId.toString(),
                ownerId = snippet.ownerId,
            )
        if (!hasPermission) {
            throw IllegalAccessException("You don't have permission to delete this test")
        }
        printScriptServiceClient.updateTestCase(id, request)
        return ResponseEntity.ok(test)
    }
}
