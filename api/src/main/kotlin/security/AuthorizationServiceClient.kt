package security

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class AuthorizationServiceClient(
    private val restTemplate: RestTemplate,
    @param:Value("\${authorization.service.url}") private val authorizationServiceUrl: String,
) {

    fun getUserSnippetIds(userId: String): List<String> {
        val url = "$authorizationServiceUrl/api/authorization/permissions/user/$userId/snippets"

        val response =
            restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                object : ParameterizedTypeReference<List<String>>() {},
            )

        return response.body ?: emptyList()
    }

    fun checkPermission(
        userId: String,
        snippetId: String,
        action: String,
        ownerId: String,
    ): Boolean {
        val url = "$authorizationServiceUrl/api/authorization/check"

        val request =
            mapOf(
                "userId" to userId,
                "snippetId" to snippetId,
                "action" to action,
                "ownerId" to ownerId,
            )

        val response =
            restTemplate.postForObject(
                url,
                request,
                CheckPermissionResponse::class.java,
            )

        return response?.allowed ?: false
    }
}

data class CheckPermissionResponse(
    val allowed: Boolean,
)
