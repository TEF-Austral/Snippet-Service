package producers

import AsyncTaskRequestContext
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import producers.strategy.AsyncTaskStrategy
import producers.strategy.TaskType
import kotlin.test.assertEquals

class AsyncTaskProducerTest {

    private lateinit var lintingStrategy: AsyncTaskStrategy
    private lateinit var formattingStrategy: AsyncTaskStrategy
    private lateinit var testingStrategy: AsyncTaskStrategy
    private lateinit var producer: AsyncTaskProducer

    @BeforeEach
    fun setup() {
        lintingStrategy = mockk(relaxed = true)
        formattingStrategy = mockk(relaxed = true)
        testingStrategy = mockk(relaxed = true)

        every { lintingStrategy.canHandle(TaskType.LINTING) } returns true
        every { lintingStrategy.canHandle(not(TaskType.LINTING)) } returns false
        every { formattingStrategy.canHandle(TaskType.FORMATTING) } returns true
        every { formattingStrategy.canHandle(not(TaskType.FORMATTING)) } returns false
        every { testingStrategy.canHandle(TaskType.TESTING) } returns true
        every { testingStrategy.canHandle(not(TaskType.TESTING)) } returns false

        producer =
            AsyncTaskProducer(
                listOf(lintingStrategy, formattingStrategy, testingStrategy),
            )
    }

    @Test
    fun `should route linting task to linting strategy`() {
        val context = createContext()
        every { lintingStrategy.submit(any()) } returns "lint-request-id"

        val requestId = producer.request(TaskType.LINTING, context)

        assertEquals("lint-request-id", requestId)
        verify { lintingStrategy.submit(context) }
        verify(exactly = 0) { formattingStrategy.submit(any()) }
        verify(exactly = 0) { testingStrategy.submit(any()) }
    }

    @Test
    fun `should route formatting task to formatting strategy`() {
        val context = createContext()
        every { formattingStrategy.submit(any()) } returns "format-request-id"

        val requestId = producer.request(TaskType.FORMATTING, context)

        assertEquals("format-request-id", requestId)
        verify { formattingStrategy.submit(context) }
        verify(exactly = 0) { lintingStrategy.submit(any()) }
        verify(exactly = 0) { testingStrategy.submit(any()) }
    }

    @Test
    fun `should route testing task to testing strategy`() {
        val context = createContext()
        every { testingStrategy.submit(any()) } returns "test-request-id"

        val requestId = producer.request(TaskType.TESTING, context)

        assertEquals("test-request-id", requestId)
        verify { testingStrategy.submit(context) }
        verify(exactly = 0) { lintingStrategy.submit(any()) }
        verify(exactly = 0) { formattingStrategy.submit(any()) }
    }

    @Test
    fun `should throw exception when no strategy found`() {
        val emptyProducer = AsyncTaskProducer(emptyList())
        val context = createContext()

        val exception =
            assertThrows<IllegalArgumentException> {
                emptyProducer.request(TaskType.LINTING, context)
            }

        assertEquals("No strategy found for task type: LINTING", exception.message)
    }

    private fun createContext() =
        AsyncTaskRequestContext(
            snippetId = 1L,
            bucketContainer = "container",
            bucketKey = "key",
            version = "1.0",
            userId = "user123",
            languageId = "PRINTSCRIPT",
        )
}
