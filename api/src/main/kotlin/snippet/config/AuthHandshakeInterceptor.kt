package snippet.config

import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor
import org.springframework.web.util.UriComponentsBuilder
import java.lang.Exception

@Component
class AuthHandshakeInterceptor : HandshakeInterceptor {

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
                println("AuthHandshakeInterceptor: Rechazando conexión. Falta snippetId o token.")
                return false
            }

            attributes["snippetId"] = snippetId
            attributes["token"] = token
            return true
        } catch (e: Exception) {
            println("AuthHandshakeInterceptor: Error al procesar la solicitud: ${e.message}")
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
