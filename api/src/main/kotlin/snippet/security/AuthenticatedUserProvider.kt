package snippet.security

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

@Component
class AuthenticatedUserProvider {

    fun getCurrentUserId(): String {
        val authentication: Authentication =
            SecurityContextHolder.getContext().authentication
                ?: throw IllegalStateException("No authentication found")

        val jwt =
            authentication.principal as? Jwt
                ?: throw IllegalStateException("Invalid authentication type")

        return jwt.getClaim<String>("sub")
            ?: jwt.subject
            ?: throw IllegalStateException("User ID not found in token")
    }

    fun getCurrentUserEmail(): String? {
        val authentication: Authentication? = SecurityContextHolder.getContext().authentication
        val jwt = authentication?.principal as? Jwt
        return jwt?.getClaim<String>("email")
    }

    fun getCurrentUserName(): String {
        val authentication: Authentication? = SecurityContextHolder.getContext().authentication
        val jwt = authentication?.principal as? Jwt
        return jwt?.getClaim("username")
            ?: throw IllegalStateException("Username not found in token")
    }

    fun getAllClaims(): Map<String, Any> {
        val authentication: Authentication? = SecurityContextHolder.getContext().authentication
        val jwt = authentication?.principal as? Jwt
        return jwt?.claims ?: emptyMap()
    }
}
