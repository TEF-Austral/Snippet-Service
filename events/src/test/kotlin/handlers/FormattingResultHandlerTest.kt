package handlers

import component.AssetService
import dtos.responses.FormattingResultEvent
import entity.Snippet
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import repositories.SnippetRepository

@ExtendWith(MockitoExtension::class)
class FormattingResultHandlerTest {

    @Mock
    lateinit var repository: SnippetRepository

    @Mock
    lateinit var assetService: AssetService

    @Test
    fun `successful formatting updates asset`() {
        val snippet =
            Snippet(
                id = 1L,
                name = "n",
                description = "d",
                ownerId = "o",
                bucketKey = "k",
                bucketContainer = "c",
                language = dtos.types.Language.PRINTSCRIPT,
                version = "1.0",
                author = "a",
            )
        `when`(repository.findById(1L)).thenReturn(java.util.Optional.of(snippet))

        val handler = FormattingResultHandler(repository, assetService)
        val evt =
            FormattingResultEvent(
                requestId = "r",
                snippetId = 1L,
                success = true,
                formattedContent = "fmt",
                error = null,
            )

        handler.handleFormattingResult(evt)

        verify(assetService).createOrUpdateAsset("c", "k", "fmt")
    }

    @Test
    fun `missing bucket key triggers exception path`() {
        val snippet =
            Snippet(
                id = 2L,
                name = "n",
                description = "d",
                ownerId = "o",
                bucketKey = null,
                bucketContainer = "c",
                language = dtos.types.Language.PRINTSCRIPT,
                version = "1.0",
                author = "a",
            )
        `when`(repository.findById(2L)).thenReturn(java.util.Optional.of(snippet))

        val handler = FormattingResultHandler(repository, assetService)
        val evt =
            FormattingResultEvent(
                requestId = "r",
                snippetId = 2L,
                success = true,
                formattedContent = "fmt",
                error = null,
            )

        handler.handleFormattingResult(evt)
        Mockito.verifyNoInteractions(assetService)
    }

    @Test
    fun `failed formatting only logs`() {
        val handler = FormattingResultHandler(repository, assetService)
        val evt =
            FormattingResultEvent(
                requestId = "r",
                snippetId = 99L,
                success = false,
                formattedContent = null,
                error = "boom",
            )

        handler.handleFormattingResult(evt)
        Mockito.verifyNoInteractions(repository)
        Mockito.verifyNoInteractions(assetService)
    }
}
