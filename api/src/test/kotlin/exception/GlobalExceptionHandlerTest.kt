package exception

import api.exception.GlobalExceptionHandler
import org.junit.jupiter.api.Test
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import kotlin.test.assertEquals

class GlobalExceptionHandlerTest {

    private val handler = GlobalExceptionHandler()

    @Test
    fun `handleNotFound returns 404 with error message when NoSuchElementException is thrown`() {
        val exception = NoSuchElementException("Snippet not found")

        val response = handler.handleNotFound(exception)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals(404, response.body?.status)
        assertEquals("Not Found", response.body?.error)
        assertEquals("Snippet not found", response.body?.message)
    }

    @Test
    fun `handleNotFound returns default message when exception message is null`() {
        val exception = NoSuchElementException()

        val response = handler.handleNotFound(exception)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals(404, response.body?.status)
        assertEquals("Not Found", response.body?.error)
        assertEquals("Resource not found", response.body?.message)
    }

    @Test
    fun `handleBadRequest - IllegalArgumentException with message`() {
        val exception = IllegalArgumentException("Invalid snippet data")

        val response = handler.handleBadRequest(exception)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals(400, response.body?.status)
        assertEquals("Bad Request", response.body?.error)
        assertEquals("Invalid snippet data", response.body?.message)
    }

    @Test
    fun `handleBadRequest returns default message when exception message is null`() {
        val exception = IllegalArgumentException()

        val response = handler.handleBadRequest(exception)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals(400, response.body?.status)
        assertEquals("Bad Request", response.body?.error)
        assertEquals("Invalid request", response.body?.message)
    }

    @Test
    fun `handleValidationErrors - MethodArgumentNotValidException with field errors`() {
        val fieldError1 = FieldError("snippetRequest", "name", "must not be blank")
        val fieldError2 = FieldError("snippetRequest", "version", "must not be null")

        val bindingResult =
            org.springframework.validation.BeanPropertyBindingResult(
                Any(),
                "snippetRequest",
            )
        bindingResult.addError(fieldError1)
        bindingResult.addError(fieldError2)

        val exception =
            MethodArgumentNotValidException(
                MethodParameter(
                    this::class.java.methods[0],
                    -1,
                ),
                bindingResult,
            )

        val response = handler.handleValidationErrors(exception)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals(400, response.body?.status)
        assertEquals("Validation Error", response.body?.error)
        assertEquals("name: must not be blank, version: must not be null", response.body?.message)
    }

    @Test
    fun `handleValidationErrors returns empty message when no field errors exist`() {
        val bindingResult =
            org.springframework.validation.BeanPropertyBindingResult(
                Any(),
                "snippetRequest",
            )

        val exception =
            MethodArgumentNotValidException(
                MethodParameter(
                    this::class.java.methods[0],
                    -1,
                ),
                bindingResult,
            )

        val response = handler.handleValidationErrors(exception)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals(400, response.body?.status)
        assertEquals("Validation Error", response.body?.error)
        assertEquals("", response.body?.message)
    }

    @Test
    fun `handleHttpClientError - NotFound`() {
        val exception =
            HttpClientErrorException.NotFound.create(
                HttpStatus.NOT_FOUND,
                "Not Found",
                org.springframework.http.HttpHeaders.EMPTY,
                ByteArray(0),
                null,
            )

        val response = handler.handleHttpClientError(exception)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals(404, response.body?.status)
        assertEquals("External Service Error", response.body?.error)
        assertEquals("Error communicating with external service: Not Found", response.body?.message)
    }

    @Test
    fun `handleHttpClientError handles 400 Bad Request from external service`() {
        val exception =
            HttpClientErrorException.BadRequest.create(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                org.springframework.http.HttpHeaders.EMPTY,
                ByteArray(0),
                null,
            )

        val response = handler.handleHttpClientError(exception)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals(400, response.body?.status)
        assertEquals("External Service Error", response.body?.error)
        assertEquals(
            "Error communicating with external service: Bad Request",
            response.body?.message,
        )
    }

    @Test
    fun `handleHttpServerError returns 503 with message when HttpServerErrorException is thrown`() {
        val exception =
            HttpServerErrorException.InternalServerError.create(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                org.springframework.http.HttpHeaders.EMPTY,
                ByteArray(0),
                null,
            )

        val response = handler.handleHttpServerError(exception)

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.statusCode)
        assertEquals(503, response.body?.status)
        assertEquals("Service Unavailable", response.body?.error)
        assertEquals(
            "External service is temporarily unavailable: Internal Server Error",
            response.body?.message,
        )
    }

    @Test
    fun `handleHttpServerError handles 502 Bad Gateway from external service`() {
        val exception =
            HttpServerErrorException.BadGateway.create(
                HttpStatus.BAD_GATEWAY,
                "Bad Gateway",
                org.springframework.http.HttpHeaders.EMPTY,
                ByteArray(0),
                null,
            )

        val response = handler.handleHttpServerError(exception)

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.statusCode)
        assertEquals(503, response.body?.status)
        assertEquals("Service Unavailable", response.body?.error)
        assertEquals(
            "External service is temporarily unavailable: Bad Gateway",
            response.body?.message,
        )
    }

    @Test
    fun `handleGenericException returns 500 with error message when generic Exception is thrown`() {
        val exception = Exception("Unexpected error occurred")

        val response = handler.handleGenericException(exception)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals(500, response.body?.status)
        assertEquals("Internal Server Error", response.body?.error)
        assertEquals("Unexpected error occurred", response.body?.message)
    }

    @Test
    fun `handleGenericException returns default message when exception message is null`() {
        val exception = Exception(null as String?)

        val response = handler.handleGenericException(exception)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals(500, response.body?.status)
        assertEquals("Internal Server Error", response.body?.error)
        assertEquals("Internal server error", response.body?.message)
    }

    @Test
    fun `handleGenericException handles RuntimeException`() {
        val exception = RuntimeException("Runtime error")

        val response = handler.handleGenericException(exception)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals(500, response.body?.status)
        assertEquals("Internal Server Error", response.body?.error)
        assertEquals("Runtime error", response.body?.message)
    }

    @Test
    fun `handleGenericException handles NullPointerException`() {
        val exception = NullPointerException("Null pointer encountered")

        val response = handler.handleGenericException(exception)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals(500, response.body?.status)
        assertEquals("Internal Server Error", response.body?.error)
        assertEquals("Null pointer encountered", response.body?.message)
    }
}
