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
            val (snippetId, token) = extractSnippetIdAndToken(request)
            validateSnippetIdAndToken(snippetId, token, response, request.uri)
            validateJwt(token!!, response)
            setAttributes(attributes, snippetId!!, token)
            log.info("WebSocket handshake accepted: snippetId=$snippetId")
            return true
        } catch (e: Exception) {
            handleException(e, response, request.uri)
            return false
        }
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?,
    ) {}

    private fun extractSnippetIdAndToken(request: ServerHttpRequest): Pair<Long?, String?> {
        val queryParams = UriComponentsBuilder.fromUri(request.uri).build().queryParams
        val snippetId = queryParams.getFirst("snippetId")?.toLongOrNull()
        val token = queryParams.getFirst("token")
        return Pair(snippetId, token)
    }

    private fun validateSnippetIdAndToken(
        snippetId: Long?,
        token: String?,
        response: ServerHttpResponse,
        uri: java.net.URI,
    ) {
        if (snippetId == null || token.isNullOrBlank()) {
            log.warn("WebSocket handshake rejected: missing snippetId or token. uri=$uri")
            response.setStatusCode(HttpStatus.FORBIDDEN)
            throw IllegalArgumentException("Missing snippetId or token")
        }
    }

    private fun validateJwt(
        token: String,
        response: ServerHttpResponse,
    ) {
        try {
            jwtDecoder.decode(token)
        } catch (e: JwtException) {
            log.warn("WebSocket handshake rejected: Invalid JWT. ${e.message}")
            response.setStatusCode(HttpStatus.FORBIDDEN)
            throw e
        }
    }

    private fun setAttributes(
        attributes: MutableMap<String, Any>,
        snippetId: Long,
        token: String,
    ) {
        attributes["snippetId"] = snippetId
        attributes["token"] = token
    }

    private fun handleException(
        e: Exception,
        response: ServerHttpResponse,
        uri: java.net.URI,
    ) {
        val stackTrace = e.stackTrace.firstOrNull()
        val location =
            stackTrace?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"
        log.error("WebSocket handshake error at $location: ${e.message}, uri=$uri", e)
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
