package api.config

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpStatus
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.web.socket.WebSocketHandler
import java.net.URI

@ExtendWith(MockitoExtension::class)
class AuthHandshakeInterceptorTest {

    @org.mockito.Mock
    lateinit var jwtDecoder: JwtDecoder

    @org.mockito.Mock
    lateinit var request: ServerHttpRequest

    @org.mockito.Mock
    lateinit var response: ServerHttpResponse

    @org.mockito.Mock
    lateinit var handler: WebSocketHandler

    private fun interceptor() = AuthHandshakeInterceptor(jwtDecoder)

    @Test
    fun `beforeHandshake rejects when params are missing`() {
        val uri = URI.create("http://localhost/ws/execute-interactive")
        `when`(request.uri).thenReturn(uri)

        val attrs = mutableMapOf<String, Any>()
        val allowed = interceptor().beforeHandshake(request, response, handler, attrs)

        assertFalse(allowed)
        verify(response).setStatusCode(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `beforeHandshake rejects when JWT is invalid`() {
        val uri = URI.create("http://localhost/ws/execute-interactive?snippetId=1&token=bad")
        `when`(request.uri).thenReturn(uri)
        `when`(jwtDecoder.decode("bad")).thenThrow(JwtException("invalid"))

        val attrs = mutableMapOf<String, Any>()
        val allowed = interceptor().beforeHandshake(request, response, handler, attrs)

        assertFalse(allowed)
        verify(response).setStatusCode(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `beforeHandshake accepts and populates attributes on valid JWT`() {
        val uri = URI.create("http://localhost/ws/execute-interactive?snippetId=42&token=good")
        `when`(request.uri).thenReturn(uri)

        val jwt = Mockito.mock(Jwt::class.java)
        `when`(jwtDecoder.decode("good")).thenReturn(jwt)

        val attrs = mutableMapOf<String, Any>()
        val allowed = interceptor().beforeHandshake(request, response, handler, attrs)

        assertTrue(allowed)
        assertTrue(attrs["snippetId"] == 42L)
        assertTrue(attrs["token"] == "good")
        verify(response, Mockito.never()).setStatusCode(org.mockito.ArgumentMatchers.any())
    }
}
