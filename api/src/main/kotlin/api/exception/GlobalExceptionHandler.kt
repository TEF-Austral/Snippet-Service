package api.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException): ResponseEntity<ErrorResponse> {
        val stackTrace = ex.stackTrace.firstOrNull()
        val location =
            stackTrace?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"
        log.warn("Resource not found at $location: ${ex.message}", ex)
        val error =
            ErrorResponse(
                status = HttpStatus.NOT_FOUND.value(),
                error = "Not Found",
                message = ex.message ?: "Resource not found",
            )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        val stackTrace = ex.stackTrace.firstOrNull()
        val location =
            stackTrace?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"
        log.warn("Bad request at $location: ${ex.message}", ex)
        val error =
            ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Bad Request",
                message = ex.message ?: "Invalid request",
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors =
            ex.bindingResult.fieldErrors.joinToString(", ") {
                "${it.field}: ${it.defaultMessage}"
            }
        val stackTrace = ex.stackTrace.firstOrNull()
        val location =
            stackTrace?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"
        log.warn("Validation error at $location: $errors", ex)
        val error =
            ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Validation Error",
                message = errors,
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(HttpClientErrorException::class)
    fun handleHttpClientError(ex: HttpClientErrorException): ResponseEntity<ErrorResponse> {
        val stackTrace = ex.stackTrace.firstOrNull()
        val location =
            stackTrace?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"
        log.error(
            "External service client error at $location (${ex.statusCode}): ${ex.statusText}",
            ex,
        )
        val error =
            ErrorResponse(
                status = ex.statusCode.value(),
                error = "External Service Error",
                message = "Error communicating with external service: ${ex.statusText}",
            )
        return ResponseEntity.status(ex.statusCode).body(error)
    }

    @ExceptionHandler(HttpServerErrorException::class)
    fun handleHttpServerError(ex: HttpServerErrorException): ResponseEntity<ErrorResponse> {
        val stackTrace = ex.stackTrace.firstOrNull()
        val location =
            stackTrace?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"
        log.error(
            "External service server error at $location (${ex.statusCode}): ${ex.statusText}",
            ex,
        )
        val error =
            ErrorResponse(
                status = HttpStatus.SERVICE_UNAVAILABLE.value(),
                error = "Service Unavailable",
                message = "External service is temporarily unavailable: ${ex.statusText}",
            )
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        val stackTrace = ex.stackTrace.firstOrNull()
        val location =
            stackTrace?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"
        log.error("Internal server error at $location: ${ex.message}", ex)
        val error =
            ErrorResponse(
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                error = "Internal Server Error",
                message = ex.message ?: "Internal server error",
            )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error)
    }

    @ExceptionHandler(IllegalAccessException::class)
    fun handleForbidden(ex: IllegalAccessException): ResponseEntity<ErrorResponse> {
        val stackTrace = ex.stackTrace.firstOrNull()
        val location =
            stackTrace?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"
        log.warn("Access denied at $location: ${ex.message}", ex)
        val error =
            ErrorResponse(
                status = HttpStatus.FORBIDDEN.value(),
                error = "Forbidden",
                message = ex.message ?: "Access denied",
            )
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error)
    }

    @ExceptionHandler(HttpClientErrorException.Unauthorized::class)
    fun handleUnauthorized(
        ex: HttpClientErrorException.Unauthorized,
    ): ResponseEntity<ErrorResponse> {
        val stackTrace = ex.stackTrace.firstOrNull()
        val location =
            stackTrace?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"
        log.warn("Unauthorized access at $location: ${ex.message}", ex)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            ErrorResponse(
                status = HttpStatus.UNAUTHORIZED.value(),
                error = "Unauthorized",
                message = ex.message ?: "Unauthorized",
            ),
        )
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleConflict(ex: IllegalStateException): ResponseEntity<ErrorResponse> {
        val stackTrace = ex.stackTrace.firstOrNull()
        val location =
            stackTrace?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"
        log.warn("Conflict at $location: ${ex.message}", ex)
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            ErrorResponse(
                status = HttpStatus.CONFLICT.value(),
                error = "Conflict",
                message = ex.message ?: "Conflict",
            ),
        )
    }
}
