package handlers.rules

import dtos.types.Language
import entity.Snippet
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import repositories.SnippetRepository
import kotlin.test.assertEquals

class SnippetPageProcessorTest {

    private lateinit var snippetRepository: SnippetRepository
    private lateinit var processor: SnippetPageProcessor

    @BeforeEach
    fun setup() {
        snippetRepository = mockk()
        processor = SnippetPageProcessor(snippetRepository)
    }

    @Test
    fun `should process all snippets in single page`() {
        val userId = "user123"
        val snippets =
            listOf(
                createSnippet(1L),
                createSnippet(2L),
                createSnippet(3L),
            )
        val page = PageImpl(snippets, PageRequest.of(0, 10), 3)

        every { snippetRepository.findByOwnerId(userId, any()) } returns page

        val processedIds = mutableListOf<Long>()
        val totalProcessed =
            processor.processAllSnippets(userId, 10) { snippet ->
                processedIds.add(snippet.id!!)
            }

        assertEquals(3, totalProcessed)
        assertEquals(listOf(1L, 2L, 3L), processedIds)
        verify(exactly = 1) { snippetRepository.findByOwnerId(userId, any()) }
    }

    @Test
    fun `should process snippets across multiple pages`() {
        val userId = "user456"
        val page1 =
            PageImpl(
                listOf(createSnippet(1L), createSnippet(2L)),
                PageRequest.of(0, 2),
                5,
            )
        val page2 =
            PageImpl(
                listOf(createSnippet(3L), createSnippet(4L)),
                PageRequest.of(1, 2),
                5,
            )
        val page3 =
            PageImpl(
                listOf(createSnippet(5L)),
                PageRequest.of(2, 2),
                5,
            )

        every { snippetRepository.findByOwnerId(userId, PageRequest.of(0, 2)) } returns page1
        every { snippetRepository.findByOwnerId(userId, PageRequest.of(1, 2)) } returns page2
        every { snippetRepository.findByOwnerId(userId, PageRequest.of(2, 2)) } returns page3

        val processedIds = mutableListOf<Long>()
        val totalProcessed =
            processor.processAllSnippets(userId, 2) { snippet ->
                processedIds.add(snippet.id!!)
            }

        assertEquals(5, totalProcessed)
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L), processedIds)
        verify(exactly = 3) { snippetRepository.findByOwnerId(userId, any()) }
    }

    @Test
    fun `should handle empty results`() {
        val userId = "user789"
        val emptyPage = PageImpl(emptyList<Snippet>(), PageRequest.of(0, 10), 0)

        every { snippetRepository.findByOwnerId(userId, any()) } returns emptyPage

        var processedCount = 0
        val totalProcessed =
            processor.processAllSnippets(userId, 10) {
                processedCount++
            }

        assertEquals(0, totalProcessed)
        assertEquals(0, processedCount)
    }

    private fun createSnippet(id: Long) =
        Snippet(
            id = id,
            name = "snippet$id",
            bucketContainer = "container",
            bucketKey = "key$id",
            ownerId = "user123",
            language = Language.PRINTSCRIPT,
            version = "1.0",
            author = "Author",
            description = "Description",
        )
}
