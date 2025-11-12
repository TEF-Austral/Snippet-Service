package controllers

import authorization.AuthorizationServiceClient
import authorization.Permissions
import common.dtos.requests.CreateTestRequestDTO
import common.dtos.responses.TestDTO
import component.PrintScriptServiceClient
import controllers.utils.getSnippet
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
    private val printScriptServiceClient: PrintScriptServiceClient,
    private val authenticatedUserProvider: AuthenticatedUserProvider,
    private val snippetRepository: SnippetRepository,
    private val authorizationServiceClient: AuthorizationServiceClient,
) {

    @PostMapping
    fun createTest(
        @RequestBody request: CreateTestRequestDTO,
    ): ResponseEntity<TestDTO> {
        val userId = authenticatedUserProvider.getCurrentUserId()

        getSnippet(
            request.snippetId,
            userId,
            Permissions.EDIT,
            snippetRepository,
            authorizationServiceClient,
        )

        val test = printScriptServiceClient.createTestCase(request)

        return ResponseEntity.status(HttpStatus.CREATED).body(test)
    }

    @GetMapping
    fun getTestsBySnippet(
        @RequestParam("snippetId") snippetId: Long,
    ): ResponseEntity<List<TestDTO>> {
        val userId = authenticatedUserProvider.getCurrentUserId()

        getSnippet(
            snippetId,
            userId,
            Permissions.READ,
            snippetRepository,
            authorizationServiceClient,
        )

        val tests = printScriptServiceClient.getTestsBySnippet(snippetId)

        return ResponseEntity.ok(tests)
    }

    @GetMapping("/{id}")
    fun getTest(
        @PathVariable id: Long,
    ): ResponseEntity<TestDTO> {
        val test = printScriptServiceClient.getTestById(id)

        val userId = authenticatedUserProvider.getCurrentUserId()

        getSnippet(
            test.snippetId,
            userId,
            Permissions.READ,
            snippetRepository,
            authorizationServiceClient,
        )

        return ResponseEntity.ok(test)
    }

    @DeleteMapping("/{id}")
    fun deleteTest(
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        val test = printScriptServiceClient.getTestById(id)

        val userId = authenticatedUserProvider.getCurrentUserId()

        getSnippet(
            test.snippetId,
            userId,
            Permissions.EDIT,
            snippetRepository,
            authorizationServiceClient,
        )

        printScriptServiceClient.deleteTestCase(id)
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/{id}")
    fun updateTest(
        @PathVariable id: Long,
        @RequestBody request: CreateTestRequestDTO,
    ): ResponseEntity<TestDTO> {
        val test = printScriptServiceClient.getTestById(id)
        val userId = authenticatedUserProvider.getCurrentUserId()

        getSnippet(
            test.snippetId,
            userId,
            Permissions.EDIT,
            snippetRepository,
            authorizationServiceClient,
        )

        printScriptServiceClient.updateTestCase(id, request)
        return ResponseEntity.ok(test)
    }
}
