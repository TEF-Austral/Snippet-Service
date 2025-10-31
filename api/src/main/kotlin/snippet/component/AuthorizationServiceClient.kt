package snippet.component

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import snippet.dtos.CheckPermissionResponseDTO
import snippet.dtos.PermissionResponseDTO

@Component
class AuthorizationServiceClient(
    private val restTemplate: RestTemplate,
    @param:Value("\${authorization.service.url}") private val authorizationServiceUrl: String,
) {

    fun checkPermission(
        userId: String,
        action: String,
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
                "action" to action,
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

    fun grantPermission(
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

    fun revokePermission(
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

    fun getSnippetPermissions(
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
}
