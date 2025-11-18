package producers.strategy

import AsyncTaskRequestContext
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import producers.LintingRequestProducer
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LintingTaskStrategyTest {

    private lateinit var producer: LintingRequestProducer
    private lateinit var strategy: LintingTaskStrategy

    @BeforeEach
    fun setup() {
        producer = mockk(relaxed = true)
        strategy = LintingTaskStrategy(producer)
    }

    @Test
    fun `should handle LINTING task type`() {
        assertTrue(strategy.canHandle(TaskType.LINTING))
    }

    @Test
    fun `should not handle other task types`() {
        assertFalse(strategy.canHandle(TaskType.FORMATTING))
        assertFalse(strategy.canHandle(TaskType.TESTING))
    }

    @Test
    fun `should throw exception when languageId is null`() {
        val context =
            AsyncTaskRequestContext(
                snippetId = 1L,
                bucketContainer = "container",
                bucketKey = "key",
                version = "1.0",
                userId = "user123",
                languageId = null,
            )

        val exception =
            assertThrows<IllegalArgumentException> {
                strategy.submit(context)
            }

        assertEquals("languageId required for linting", exception.message)
    }

    @Test
    fun `should throw exception when userId is null`() {
        val context =
            AsyncTaskRequestContext(
                snippetId = 1L,
                bucketContainer = "container",
                bucketKey = "key",
                version = "1.0",
                userId = null,
                languageId = "PRINTSCRIPT",
            )

        val exception =
            assertThrows<IllegalArgumentException> {
                strategy.submit(context)
            }

        assertEquals("userId required for linting", exception.message)
    }

    @Test
    fun `should generate unique request IDs`() {
        val context =
            AsyncTaskRequestContext(
                snippetId = 1L,
                bucketContainer = "container",
                bucketKey = "key",
                version = "1.0",
                userId = "user123",
                languageId = "PRINTSCRIPT",
            )

        val requestId1 = strategy.submit(context)
        val requestId2 = strategy.submit(context)

        assertTrue(requestId1 != requestId2)
    }
}
