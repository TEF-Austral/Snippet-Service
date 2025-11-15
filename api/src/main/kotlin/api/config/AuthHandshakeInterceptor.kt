package api.config

import org.springframework.http.HttpStatus
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtException // 1. Importar la excepción
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor
import java.net.URLDecoder // 2. Importar el decodificador
import java.nio.charset.StandardCharsets // 3. Importar el charset

@Component
class AuthHandshakeInterceptor(
    private val jwtDecoder: JwtDecoder,
) : HandshakeInterceptor {
    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>,
    ): Boolean {
        try {
            val uri = request.uri
            val query = uri.query ?: ""

            // --- INICIO DE CORRECCIÓN: Decodificar la URL ---
            val params =
                query
                    .split('&')
                    .mapNotNull {
                        val parts = it.split('=', limit = 2)
                        if (parts.size == 2) {
                            val key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8.name())
                            val value = URLDecoder.decode(parts[1], StandardCharsets.UTF_8.name())
                            key to value
                        } else {
                            null
                        }
                    }.associate { it }
            // --- FIN DE CORRECCIÓN ---

            val token = params["token"]

            // --- INICIO DE CORRECCIÓN: Validación de JWT ---
            if (token == null) {
                response.setStatusCode(HttpStatus.FORBIDDEN)
                return false
            }

            // Validación estándar: Intenta decodificar.
            // Si falla (expirado, firma mala, etc.), lanza JwtException.
            jwtDecoder.decode(token)

            // Si llegamos aquí, el token es válido.
            attributes["token"] = token
            return true
        } catch (e: JwtException) {
            // El token es inválido
            response.setStatusCode(HttpStatus.FORBIDDEN)
            return false
        } catch (e: Exception) {
            // Otro error (ej. parsing de URL)
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
        // No se necesita implementación
    }
}
