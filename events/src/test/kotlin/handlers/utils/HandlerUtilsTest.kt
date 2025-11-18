package handlers.utils

import dtos.types.ComplianceStatus
import dtos.responses.LintingResultEvent
import dtos.responses.ViolationDTO
import entity.Snippet
import dtos.types.Language
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import repositories.SnippetRepository
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class HandlerUtilsTest {

    private val snippetRepository: SnippetRepository = mockk()
    private lateinit var snippet: Snippet

    @BeforeEach
    fun setUp() {
        // Create a real Snippet instance to avoid mocking data class copy behavior
        snippet =
            Snippet(
                id = 1L,
                name = "name",
                description = "desc",
                ownerId = "owner",
                bucketKey = "key",
                bucketContainer = "snippets",
                language = Language.PRINTSCRIPT,
                version = "1.0.0",
                author = "author",
                complianceStatus = ComplianceStatus.PENDING,
                lastValidationError = null,
            )
    }

    @Test
    fun `findSnippetOrThrow should return snippet when found`() {
        every { snippetRepository.findById(1L) } returns Optional.of(snippet)

        val result = findSnippetOrThrow(snippetRepository, 1L)

        assertEquals(snippet, result)
        verify { snippetRepository.findById(1L) }
    }

    @Test
    fun `findSnippetOrThrow should throw NoSuchElementException when not found`() {
        every { snippetRepository.findById(1L) } returns Optional.empty()

        val exception =
            assertFailsWith<NoSuchElementException> {
                findSnippetOrThrow(snippetRepository, 1L)
            }

        assertEquals("Snippet not found: 1", exception.message)
    }

    @Test
    fun `handleLintingSnippetResult should return COMPLIANT when result is valid`() {
        val lintingResult =
            LintingResultEvent(
                snippetId = 1L,
                requestId = "req-1",
                isValid = true,
                violations = emptyList(),
            )

        val updatedSnippet = handleLintingSnippetResult(snippet, lintingResult)

        assertEquals(ComplianceStatus.COMPLIANT, updatedSnippet.complianceStatus)
        assertNull(updatedSnippet.lastValidationError)
    }

    @Test
    fun `handleLintingSnippetResult should errors when result is invalid`() {
        val violations =
            listOf(
                ViolationDTO(line = 10, column = 5, message = "Error 1"),
                ViolationDTO(line = 20, column = 1, message = "Error 2"),
            )
        val lintingResult =
            LintingResultEvent(
                snippetId = 1L,
                requestId = "req-1",
                isValid = false,
                violations = violations,
            )

        val expectedError =
            """
            Line 10, Column 5: Error 1
            Line 20, Column 1: Error 2
            """.trimIndent()

        val updatedSnippet = handleLintingSnippetResult(snippet, lintingResult)

        assertEquals(ComplianceStatus.NON_COMPLIANT, updatedSnippet.complianceStatus)
        assertEquals(expectedError, updatedSnippet.lastValidationError)
    }
}
