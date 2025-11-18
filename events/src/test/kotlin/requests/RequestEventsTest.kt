package requests

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class RequestEventsTest {

    @Test
    fun `test LintingRequestEvent data class methods`() {
        val event1 =
            LintingRequestEvent(
                requestId = "req-1",
                bucketContainer = "container",
                bucketKey = "key",
                version = "1.0",
                languageId = "kotlin",
                userId = "user-1",
                snippetId = 1L,
            )
        val event2 = event1.copy(requestId = "req-2")

        assertEquals("req-1", event1.requestId)
        assertEquals(event1.hashCode(), event1.hashCode())
        assertEquals(event1, event1)
        assertNotEquals(event1, event2)
        assertNotEquals(event1.hashCode(), event2.hashCode())
        assertTrue(event1.toString().contains("req-1"))
    }

    @Test
    fun `test FormattingRequestEvent data class methods`() {
        val event1 =
            FormattingRequestEvent(
                requestId = "req-1",
                bucketContainer = "container",
                bucketKey = "key",
                languageId = "kotlin",
                version = "1.0",
                userId = "user-1",
                snippetId = 1L,
            )
        val event2 = event1.copy(requestId = "req-2")

        assertEquals("req-1", event1.requestId)
        assertEquals(event1.hashCode(), event1.hashCode())
        assertEquals(event1, event1)
        assertNotEquals(event1, event2)
        assertNotEquals(event1.hashCode(), event2.hashCode())
        assertTrue(event1.toString().contains("req-1"))
    }

    @Test
    fun `test TestingRequestEvent data class methods`() {
        val event1 =
            TestingRequestEvent(
                requestId = "req-1",
                snippetId = 1L,
                bucketContainer = "container",
                bucketKey = "key",
                version = "1.0",
            )
        val event2 = event1.copy(requestId = "req-2")

        assertEquals("req-1", event1.requestId)
        assertEquals(event1.hashCode(), event1.hashCode())
        assertEquals(event1, event1)
        assertNotEquals(event1, event2)
        assertNotEquals(event1.hashCode(), event2.hashCode())
        assertTrue(event1.toString().contains("req-1"))
    }
}
