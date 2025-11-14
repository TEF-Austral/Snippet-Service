package security

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

@Component
class AuthenticatedUserProvider : AuthenticatedUserProviderInt {

    override fun getCurrentUserId(): String {
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

    override fun getCurrentUserName(): String? {
        val authentication: Authentication =
            SecurityContextHolder.getContext().authentication
                ?: return null

        val jwt = authentication.principal as? Jwt ?: return null

        return jwt.getClaim("username") as? String
            ?: jwt.getClaim("preferred_username") as? String
            ?: jwt.getClaim("email") as? String
            ?: jwt.subject
    }
}
