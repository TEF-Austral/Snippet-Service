package handlers

import common.dtos.types.ComplianceStatus
import dtos.types.Language
import dtos.responses.LintingResultEvent
import dtos.responses.ViolationDTO
import entity.Snippet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import repositories.SnippetRepository

@ExtendWith(MockitoExtension::class)
class LintingResultHandlerTest {

    @Mock
    lateinit var repository: SnippetRepository

    @Test
    fun `valid linting marks snippet compliant`() {
        val existing =
            Snippet(
                id = 1L,
                name = "n",
                description = "d",
                ownerId = "o",
                bucketKey = "k",
                bucketContainer = "c",
                language = Language.PRINTSCRIPT,
                version = "1.0",
                author = "a",
            )
        `when`(repository.findById(1L)).thenReturn(java.util.Optional.of(existing))

        val handler = LintingResultHandler(repository)
        val evt = LintingResultEvent("r", 1L, true)

        handler.handleLintingResult(evt)

        val captor = ArgumentCaptor.forClass(Snippet::class.java)
        verify(repository).save(captor.capture())
        assertEquals(ComplianceStatus.COMPLIANT, captor.value.complianceStatus)
        assertEquals(null, captor.value.lastValidationError)
    }

    @Test
    fun `invalid linting marks snippet non-compliant with errors`() {
        val existing =
            Snippet(
                id = 2L,
                name = "n",
                description = "d",
                ownerId = "o",
                bucketKey = "k",
                bucketContainer = "c",
                language = Language.PRINTSCRIPT,
                version = "1.0",
                author = "a",
            )
        `when`(repository.findById(2L)).thenReturn(java.util.Optional.of(existing))

        val handler = LintingResultHandler(repository)
        val evt = LintingResultEvent("r", 2L, false, listOf(ViolationDTO("oops", 1, 1)))

        handler.handleLintingResult(evt)

        val captor = ArgumentCaptor.forClass(Snippet::class.java)
        verify(repository).save(captor.capture())
        assertEquals(ComplianceStatus.NON_COMPLIANT, captor.value.complianceStatus)
    }
}
