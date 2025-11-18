package exception

import api.exception.GlobalExceptionHandler
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import kotlin.test.assertEquals

class GlobalExceptionHandlerAdditionalTest {

    private val handler = GlobalExceptionHandler()

    @Test
    fun `handleGenericException returns 500 with message`() {
        val ex = Exception("boom")
        val response = handler.handleGenericException(ex)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals(500, response.body?.status)
        assertEquals("Internal Server Error", response.body?.error)
        assertEquals("boom", response.body?.message)
    }

    @Test
    fun `handleForbidden returns 403`() {
        val ex = IllegalAccessException("nope")
        val response = handler.handleForbidden(ex)
        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
        assertEquals(403, response.body?.status)
        assertEquals("Forbidden", response.body?.error)
        assertEquals("nope", response.body?.message)
    }

    @Test
    fun `handleUnauthorized returns 401`() {
        val ex =
            HttpClientErrorException.Unauthorized.create(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                org.springframework.http.HttpHeaders.EMPTY,
                ByteArray(0),
                null,
            )
        val response = handler.handleUnauthorized(ex as HttpClientErrorException.Unauthorized)
        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        assertEquals(401, response.body?.status)
        assertEquals("Unauthorized", response.body?.error)
    }

    @Test
    fun `handleConflict returns 409`() {
        val ex = IllegalStateException("conflict")
        val response = handler.handleConflict(ex)
        assertEquals(HttpStatus.CONFLICT, response.statusCode)
        assertEquals(409, response.body?.status)
        assertEquals("Conflict", response.body?.error)
        assertEquals("conflict", response.body?.message)
    }
}
