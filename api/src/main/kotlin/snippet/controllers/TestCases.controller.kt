package snippet.controllers

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import snippet.security.AuthenticatedUserProvider
import snippet.repositories.SnippetRepository
import snippet.component.AuthorizationServiceClient
import snippet.dtos.requests.CreateTestRequestDTO
import snippet.dtos.TestDTO

@RestController
@RequestMapping("/testcases")
class TestCasesController(
    private val restTemplate: RestTemplate,
    private val authenticatedUserProvider: AuthenticatedUserProvider,
    private val snippetRepository: SnippetRepository,
    private val authorizationServiceClient: AuthorizationServiceClient,
    @param:Value("\${printscript.service.url}") private val printScriptServiceUrl: String,
) {

    @PostMapping
    fun createTest(
        @RequestBody request: CreateTestRequestDTO,
    ): ResponseEntity<TestDTO> {
        val userId = authenticatedUserProvider.getCurrentUserId()
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
            throw IllegalAccessException(
                "You don't have permission to create tests for this snippet",
            )
        }

        val url = "$printScriptServiceUrl/tests"
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

        val requestEntity = HttpEntity(request, headers)

        val response = restTemplate.postForEntity(url, requestEntity, TestDTO::class.java)

        return ResponseEntity.status(HttpStatus.CREATED).body(response.body)
    }

    @GetMapping
    fun getTestsBySnippet(
        @RequestParam("snippetId") snippetId: Long,
    ): ResponseEntity<List<TestDTO>> {
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
            throw IllegalAccessException("You don't have permission to view tests for this snippet")
        }

        val url = "$printScriptServiceUrl/tests?snippetId=$snippetId"

        val response =
            restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                Array<TestDTO>::class.java,
            )

        return ResponseEntity.ok(response.body?.toList() ?: emptyList())
    }

    @GetMapping("/{id}")
    fun getTest(
        @PathVariable id: Long,
    ): ResponseEntity<TestDTO> {
        val testUrl = "$printScriptServiceUrl/tests/$id"
        val test =
            restTemplate.getForObject(testUrl, TestDTO::class.java)
                ?: throw NoSuchElementException("Test not found: $id")

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
            throw IllegalAccessException("You don't have permission to view this test")
        }

        return ResponseEntity.ok(test)
    }

    @DeleteMapping("/{id}")
    fun deleteTest(
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        val testUrl = "$printScriptServiceUrl/tests/$id"
        val test =
            restTemplate.getForObject(testUrl, TestDTO::class.java)
                ?: throw NoSuchElementException("Test not found: $id")

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

        restTemplate.delete(testUrl)
        return ResponseEntity.noContent().build()
    }
}
