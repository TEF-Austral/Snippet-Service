package handlers.rules

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RulesUpdatedHandlerTest {

    private lateinit var handler: RulesUpdatedHandler
    private lateinit var lintHandler: RuleUpdateHandleInt
    private lateinit var formatHandler: RuleUpdateHandleInt

    @BeforeEach
    fun setup() {
        lintHandler = mockk(relaxed = true)
        formatHandler = mockk(relaxed = true)

        every { lintHandler.canHandle(RuleType.Lint) } returns true
        every { lintHandler.canHandle(RuleType.Format) } returns false
        every { formatHandler.canHandle(RuleType.Format) } returns true
        every { formatHandler.canHandle(RuleType.Lint) } returns false

        handler = RulesUpdatedHandler(listOf(lintHandler, formatHandler))
    }

    @Test
    fun `should handle lint rule type`() {
        val userId = "user123"

        handler.handle(RuleType.Lint, userId)

        verify { lintHandler.handle(userId, any()) }
        verify(exactly = 0) { formatHandler.handle(any(), any()) }
    }

    @Test
    fun `should handle format rule type`() {
        val userId = "user456"

        handler.handle(RuleType.Format, userId)

        verify { formatHandler.handle(userId, any()) }
        verify(exactly = 0) { lintHandler.handle(any(), any()) }
    }

    @Test
    fun `should handle when no handler is found`() {
        val emptyHandler = RulesUpdatedHandler(emptyList())

        // Should not throw, just log warning
        emptyHandler.handle(RuleType.Lint, "user789")
    }
}
