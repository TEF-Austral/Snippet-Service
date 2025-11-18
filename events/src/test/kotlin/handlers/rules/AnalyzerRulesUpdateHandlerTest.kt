package handlers.rules

import dtos.types.Language
import entity.Snippet
import io.mockk.mockk
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import producers.AsyncTaskProducerInt
import producers.strategy.TaskType
import repositories.SnippetRepository
import kotlin.test.assertTrue

class AnalyzerRulesUpdateHandlerTest {

    private lateinit var snippetRepository: SnippetRepository
    private lateinit var asyncTaskProducer: AsyncTaskProducerInt
    private lateinit var pageProcessor: SnippetPageProcessor
    private lateinit var handler: AnalyzerRulesUpdateHandler

    @BeforeEach
    fun setup() {
        snippetRepository = mockk()
        asyncTaskProducer = mockk(relaxed = true)
        pageProcessor = mockk(relaxed = true)

        handler =
            AnalyzerRulesUpdateHandler(
                snippetRepository,
                asyncTaskProducer,
                pageProcessor,
            )
    }

    @Test
    fun `should handle Lint rule type`() {
        assertTrue(handler.canHandle(RuleType.Lint))
    }

    @Test
    fun `should not handle Format rule type`() {
        assertTrue(!handler.canHandle(RuleType.Format))
    }

    @Test
    fun `should process all snippets and send linting requests`() {
        val userId = "user123"
        val snippets =
            listOf(
                createSnippet(1L),
                createSnippet(2L),
            )

        every {
            pageProcessor.processAllSnippets(userId, any(), any())
        } answers {
            val processSnippet = arg<(Snippet) -> Unit>(2)
            snippets.forEach { processSnippet(it) }
            snippets.size.toLong()
        }

        every { asyncTaskProducer.request(any(), any()) } returns "request-id"

        handler.handle(userId)

        verify(exactly = 2) {
            asyncTaskProducer.request(TaskType.LINTING, any())
        }
    }

    @Test
    fun `should handle error when sending linting request fails`() {
        val userId = "user456"
        val snippet = createSnippet(1L)

        every {
            pageProcessor.processAllSnippets(userId, any(), any())
        } answers {
            val processSnippet = arg<(Snippet) -> Unit>(2)
            processSnippet(snippet)
            1L
        }

        every {
            asyncTaskProducer.request(TaskType.LINTING, any())
        } throws RuntimeException("Connection failed")

        // Should not throw, just log error
        handler.handle(userId)

        verify { asyncTaskProducer.request(TaskType.LINTING, any()) }
    }

    @Test
    fun `should handle critical error during processing`() {
        val userId = "user789"

        every {
            pageProcessor.processAllSnippets(userId, any(), any())
        } throws RuntimeException("Database error")

        // Should not throw, just log error
        handler.handle(userId)
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
