package api.exception

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertNotEquals

class ErrorResponseTest {

    @Test
    fun `ErrorResponse creates instance with all fields`() {
        val errorResponse =
            ErrorResponse(
                status = 404,
                error = "Not Found",
                message = "Resource not found",
            )

        assertEquals(404, errorResponse.status)
        assertEquals("Not Found", errorResponse.error)
        assertEquals("Resource not found", errorResponse.message)
    }

    @Test
    fun `ErrorResponse equals works correctly`() {
        val errorResponse1 = ErrorResponse(404, "Not Found", "Resource not found")
        val errorResponse2 = ErrorResponse(404, "Not Found", "Resource not found")
        val errorResponse3 = ErrorResponse(500, "Internal Error", "Server error")

        assertEquals(errorResponse1, errorResponse2)
        assertNotEquals(errorResponse1, errorResponse3)
    }

    @Test
    fun `ErrorResponse hashCode works correctly`() {
        val errorResponse1 = ErrorResponse(404, "Not Found", "Resource not found")
        val errorResponse2 = ErrorResponse(404, "Not Found", "Resource not found")

        assertEquals(errorResponse1.hashCode(), errorResponse2.hashCode())
    }

    @Test
    fun `ErrorResponse toString contains all fields`() {
        val errorResponse = ErrorResponse(404, "Not Found", "Resource not found")
        val toString = errorResponse.toString()

        assertTrue(toString.contains("404"))
        assertTrue(toString.contains("Not Found"))
        assertTrue(toString.contains("Resource not found"))
    }

    @Test
    fun `ErrorResponse copy works correctly`() {
        val original = ErrorResponse(404, "Not Found", "Resource not found")
        val copied = original.copy(status = 500, error = "Internal Error")

        assertEquals(500, copied.status)
        assertEquals("Internal Error", copied.error)
        assertEquals("Resource not found", copied.message)
        assertEquals(404, original.status)
    }

    @Test
    fun `ErrorResponse component functions work correctly`() {
        val errorResponse = ErrorResponse(404, "Not Found", "Resource not found")

        val (status, error, message) = errorResponse

        assertEquals(404, status)
        assertEquals("Not Found", error)
        assertEquals("Resource not found", message)
    }
}
