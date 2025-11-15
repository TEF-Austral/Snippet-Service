package api.config

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor
import org.springframework.web.util.UriComponentsBuilder
import java.lang.Exception

@Component
class AuthHandshakeInterceptor(
    private val jwtDecoder: JwtDecoder,
) : HandshakeInterceptor {
    private val log = LoggerFactory.getLogger(AuthHandshakeInterceptor::class.java)

    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>,
    ): Boolean {
        try {
            val queryParams = UriComponentsBuilder.fromUri(request.uri).build().queryParams

            val snippetId = queryParams.getFirst("snippetId")?.toLongOrNull()
            val token = queryParams.getFirst("token")

            if (snippetId == null || token.isNullOrBlank()) {
                log.warn(
                    "WebSocket handshake rejected: missing snippetId or token. uri=${request.uri}",
                )
                response.setStatusCode(HttpStatus.FORBIDDEN)
                return false
            }

            try {
                jwtDecoder.decode(token)
            } catch (e: JwtException) {
                log.warn("WebSocket handshake rejected: Invalid JWT. ${e.message}")
                response.setStatusCode(HttpStatus.FORBIDDEN)
                return false
            }

            attributes["snippetId"] = snippetId
            attributes["token"] = token
            log.info("WebSocket handshake accepted: snippetId=$snippetId")
            return true
        } catch (e: Exception) {
            val stackTrace = e.stackTrace.firstOrNull()
            val location =
                stackTrace?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"
            log.error("WebSocket handshake error at $location: ${e.message}, uri=${request.uri}", e)
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR)
            return false
        }
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?,
    ) {
        // No action needed after handshake
    }
}
