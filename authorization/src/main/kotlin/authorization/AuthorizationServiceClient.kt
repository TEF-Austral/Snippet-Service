package authorization

import dtos.responses.CheckPermissionResponseDTO
import dtos.responses.PermissionResponseDTO
import entity.Snippet
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class AuthorizationServiceClient(
    private val restTemplate: RestTemplate,
    @param:Value($$"${authorization.service.url}") private val authorizationServiceUrl: String,
) : AuthorizationService {

    override fun checkPermission(
        userId: String,
        action: UserAction,
        snippetId: String,
        ownerId: String,
    ): Boolean {
        val url = "$authorizationServiceUrl/api/authorization/check"

        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

        val requestBody =
            mapOf(
                "userId" to userId,
                "action" to action.toString().lowercase(),
                "snippetId" to snippetId,
                "ownerId" to ownerId,
            )

        val request = HttpEntity(requestBody, headers)

        return try {
            val response =
                restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    CheckPermissionResponseDTO::class.java,
                )
            response.body?.allowed ?: false
        } catch (e: Exception) {
            false
        }
    }

    override fun grantPermission(
        requesterId: String,
        ownerId: String,
        granteeId: String,
        snippetId: String,
        canRead: Boolean,
        canEdit: Boolean,
    ): PermissionResponseDTO {
        val url = "$authorizationServiceUrl/api/authorization/permissions"

        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

        val requestBody =
            mapOf(
                "requesterId" to requesterId,
                "ownerId" to ownerId,
                "granteeId" to granteeId,
                "snippetId" to snippetId,
                "canRead" to canRead,
                "canEdit" to canEdit,
            )

        val request = HttpEntity(requestBody, headers)

        val response =
            restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                PermissionResponseDTO::class.java,
            )

        return response.body ?: throw IllegalStateException("Failed to grant permission")
    }

    override fun revokePermission(
        requesterId: String,
        userId: String,
        snippetId: String,
    ) {
        val url = "$authorizationServiceUrl/api/authorization/permissions/revoke"

        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

        val requestBody =
            mapOf(
                "requesterId" to requesterId,
                "userId" to userId,
                "snippetId" to snippetId,
            )

        val request = HttpEntity(requestBody, headers)

        restTemplate.exchange(
            url,
            HttpMethod.POST,
            request,
            Void::class.java,
        )
    }

    override fun getSnippetPermissions(
        requesterId: String,
        snippetId: String,
    ): List<PermissionResponseDTO> {
        val url = "$authorizationServiceUrl/api/authorization/permissions/snippet"

        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

        val requestBody =
            mapOf(
                "requesterId" to requesterId,
                "snippetId" to snippetId,
            )

        val request = HttpEntity(requestBody, headers)

        val response =
            restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                Array<PermissionResponseDTO>::class.java,
            )

        return response.body?.toList() ?: emptyList()
    }

    override fun getSnippetsByPermission(
        userId: String,
        permission: String,
    ): List<String> {
        val url =
            "$authorizationServiceUrl/api/authorization/snippets/" +
                "by-permission?userId=$userId&permission=$permission"

        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

        val request = HttpEntity<Void>(headers)

        val response =
            restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                Array<String>::class.java,
            )

        return response.body?.toList() ?: emptyList()
    }

    override fun checkReadPermission(
        requesterId: String,
        id: Long,
        snippet: Snippet?,
    ) {
        if (snippet == null) {
            throw NoSuchElementException("Snippet not found: $id")
        }

        val hasPermission =
            checkPermission(
                userId = requesterId,
                action = UserAction.READ,
                snippetId = id.toString(),
                ownerId = snippet.ownerId,
            )

        if (!hasPermission) {
            throw IllegalAccessException("You don't have permission to access this snippet")
        }
    }
}
