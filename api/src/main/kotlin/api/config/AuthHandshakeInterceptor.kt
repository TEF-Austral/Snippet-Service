package api.config

import org.slf4j.LoggerFactory
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor
import org.springframework.web.util.UriComponentsBuilder
import java.lang.Exception

@Component
class AuthHandshakeInterceptor : HandshakeInterceptor {
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
            return false
        }
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?,
    ) {
    }
}
