package controllers

import authorization.AuthorizationServiceClient
import authorization.UserAction
import controllers.utils.getSnippet
import dtos.requests.CreateTestRequestDTO
import dtos.responses.TestDTO
import language.LanguageServiceClientInt
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
import repositories.SnippetRepository
import security.AuthenticatedUserProvider

@RestController
@RequestMapping("/testcases")
class TestCasesController(
    private val executionServiceClient: LanguageServiceClientInt,
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
        log.info("Creating test case: snippetId=${request.snippetId}, userId=$userId")

        getSnippet(
            request.snippetId,
            userId,
            UserAction.EDIT,
            snippetRepository,
            authorizationServiceClient,
        )

        val test = executionServiceClient.createTestCase(request)

        log.info(
            "Test case created successfully: testId=${test.id}, snippetId=${request.snippetId}",
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(test)
    }

    @GetMapping
    fun getTestsBySnippet(
        @RequestParam("snippetId") snippetId: Long,
    ): ResponseEntity<List<TestDTO>> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        log.info("Getting test cases for snippet: snippetId=$snippetId, userId=$userId")

        getSnippet(
            snippetId,
            userId,
            UserAction.READ,
            snippetRepository,
            authorizationServiceClient,
        )

        val tests = executionServiceClient.getTestsBySnippet(snippetId)

        log.info("Retrieved ${tests.size} test cases for snippet: snippetId=$snippetId")
        return ResponseEntity.ok(tests)
    }

    @GetMapping("/{id}")
    fun getTest(
        @PathVariable id: Long,
    ): ResponseEntity<TestDTO> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        log.info("Getting test case: testId=$id, userId=$userId")

        val test = executionServiceClient.getTestById(id)

        getSnippet(
            test.snippetId,
            userId,
            UserAction.READ,
            snippetRepository,
            authorizationServiceClient,
        )

        log.info("Test case retrieved: testId=$id, snippetId=${test.snippetId}")
        return ResponseEntity.ok(test)
    }

    @DeleteMapping("/{id}")
    fun deleteTest(
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        log.info("Deleting test case: testId=$id, userId=$userId")

        val test = executionServiceClient.getTestById(id)

        getSnippet(
            test.snippetId,
            userId,
            UserAction.EDIT,
            snippetRepository,
            authorizationServiceClient,
        )

        executionServiceClient.deleteTestCase(id)

        log.info("Test case deleted successfully: testId=$id, snippetId=${test.snippetId}")
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/{id}")
    fun updateTest(
        @PathVariable id: Long,
        @RequestBody request: CreateTestRequestDTO,
    ): ResponseEntity<TestDTO> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        log.info("Updating test case: testId=$id, userId=$userId")

        val test = executionServiceClient.getTestById(id)

        getSnippet(
            test.snippetId,
            userId,
            UserAction.EDIT,
            snippetRepository,
            authorizationServiceClient,
        )

        executionServiceClient.updateTestCase(id, request)
        val updatedTest = executionServiceClient.getTestById(id)

        log.info("Test case updated successfully: testId=$id, snippetId=${test.snippetId}")
        return ResponseEntity.ok(updatedTest)
    }
}
