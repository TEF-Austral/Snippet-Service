package snippet.component

import authorization.AuthorizationServiceClient
import authorization.UserAction
import dtos.responses.CheckPermissionResponseDTO
import dtos.responses.PermissionResponseDTO
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq

@ExtendWith(MockitoExtension::class)
class AuthorizationServiceClientTest {

    @Mock
    private lateinit var restTemplate: RestTemplate

    private lateinit var authorizationServiceClient: AuthorizationServiceClient

    private val authorizationServiceUrl = "http://authorization-service"

    @BeforeEach
    fun setup() {
        authorizationServiceClient =
            AuthorizationServiceClient(restTemplate, authorizationServiceUrl)
    }

    @Test
    fun `checkPermission should return true when permission is granted`() {
        val userId = "user123"
        val action = UserAction.READ
        val snippetId = "1"
        val ownerId = "owner123"
        val url = "$authorizationServiceUrl/api/authorization/check"
        val response = CheckPermissionResponseDTO(allowed = true)

        `when`(
            restTemplate.exchange(
                eq(url),
                eq(HttpMethod.POST),
                any(HttpEntity::class.java),
                eq(CheckPermissionResponseDTO::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(response))

        val result = authorizationServiceClient.checkPermission(userId, action, snippetId, ownerId)

        assertTrue(result)
    }

    @Test
    fun `checkPermission should return false when permission is denied`() {
        val userId = "user123"
        val action = UserAction.EDIT
        val snippetId = "1"
        val ownerId = "owner123"
        val url = "$authorizationServiceUrl/api/authorization/check"
        val response = CheckPermissionResponseDTO(allowed = false)

        authorizationServiceClient =
            AuthorizationServiceClient(restTemplate, authorizationServiceUrl)

        `when`(
            restTemplate.exchange(
                eq(url),
                eq(HttpMethod.POST),
                any(HttpEntity::class.java),
                eq(CheckPermissionResponseDTO::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(response))

        val result = authorizationServiceClient.checkPermission(userId, action, snippetId, ownerId)

        assertFalse(result)
    }

    @Test
    fun `checkPermission should return false when exception occurs`() {
        val userId = "user123"
        val action = UserAction.DELETE
        val snippetId = "1"
        val ownerId = "owner123"
        val url = "$authorizationServiceUrl/api/authorization/check"

        authorizationServiceClient =
            AuthorizationServiceClient(restTemplate, authorizationServiceUrl)

        `when`(
            restTemplate.exchange(
                eq(url),
                eq(HttpMethod.POST),
                any(HttpEntity::class.java),
                eq(CheckPermissionResponseDTO::class.java),
            ),
        ).thenThrow(RuntimeException("Service unavailable"))

        val result = authorizationServiceClient.checkPermission(userId, action, snippetId, ownerId)

        assertFalse(result)
    }

    @Test
    fun `checkPermission should return false when response body is null`() {
        val userId = "user123"
        val action = UserAction.READ
        val snippetId = "1"
        val ownerId = "owner123"
        val url = "$authorizationServiceUrl/api/authorization/check"

        authorizationServiceClient =
            AuthorizationServiceClient(restTemplate, authorizationServiceUrl)

        `when`(
            restTemplate.exchange(
                eq(url),
                eq(HttpMethod.POST),
                any(HttpEntity::class.java),
                eq(CheckPermissionResponseDTO::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(null))

        val result = authorizationServiceClient.checkPermission(userId, action, snippetId, ownerId)

        assertFalse(result)
    }

    @Test
    fun `grantPermission should call REST API with correct parameters`() {
        val requesterId = "requester123"
        val ownerId = "owner123"
        val granteeId = "grantee456"
        val snippetId = "1"
        val canRead = true
        val canEdit = false
        val url = "$authorizationServiceUrl/api/authorization/permissions"
        val response =
            PermissionResponseDTO(
                id = 1L,
                snippetId = snippetId,
                userId = granteeId,
                canRead = canRead,
                canEdit = canEdit,
            )

        authorizationServiceClient =
            AuthorizationServiceClient(restTemplate, authorizationServiceUrl)

        `when`(
            restTemplate.exchange(
                eq(url),
                eq(HttpMethod.POST),
                any(HttpEntity::class.java),
                eq(PermissionResponseDTO::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(response))

        val result =
            authorizationServiceClient.grantPermission(
                requesterId,
                ownerId,
                granteeId,
                snippetId,
                canRead,
                canEdit,
            )

        assertEquals(snippetId, result.snippetId)
        assertEquals(granteeId, result.userId)
        assertEquals(canRead, result.canRead)
        assertEquals(canEdit, result.canEdit)
    }

    @Test
    fun `grantPermission should grant full permissions`() {
        val requesterId = "requester123"
        val ownerId = "owner123"
        val granteeId = "grantee456"
        val snippetId = "1"
        val url = "$authorizationServiceUrl/api/authorization/permissions"
        val response =
            PermissionResponseDTO(
                id = 1L,
                snippetId = snippetId,
                userId = granteeId,
                canRead = true,
                canEdit = true,
            )

        authorizationServiceClient =
            AuthorizationServiceClient(restTemplate, authorizationServiceUrl)

        `when`(
            restTemplate.exchange(
                eq(url),
                eq(HttpMethod.POST),
                any(HttpEntity::class.java),
                eq(PermissionResponseDTO::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(response))

        val result =
            authorizationServiceClient.grantPermission(
                requesterId,
                ownerId,
                granteeId,
                snippetId,
                true,
                true,
            )

        assertTrue(result.canRead)
        assertTrue(result.canEdit)
    }

    @Test
    fun `grantPermission should throw exception when response body is null`() {
        val requesterId = "requester123"
        val ownerId = "owner123"
        val granteeId = "grantee456"
        val snippetId = "1"
        val url = "$authorizationServiceUrl/api/authorization/permissions"

        authorizationServiceClient =
            AuthorizationServiceClient(restTemplate, authorizationServiceUrl)

        `when`(
            restTemplate.exchange(
                eq(url),
                eq(HttpMethod.POST),
                any(HttpEntity::class.java),
                eq(PermissionResponseDTO::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(null))

        try {
            authorizationServiceClient.grantPermission(
                requesterId,
                ownerId,
                granteeId,
                snippetId,
                true,
                false,
            )
            throw AssertionError("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            assertEquals("Failed to grant permission", e.message)
        }
    }

    @Test
    fun `revokePermission should call REST API with correct parameters`() {
        val requesterId = "requester123"
        val userId = "user456"
        val snippetId = "1"
        val url = "$authorizationServiceUrl/api/authorization/permissions/revoke"

        authorizationServiceClient =
            AuthorizationServiceClient(restTemplate, authorizationServiceUrl)

        `when`(
            restTemplate.exchange(
                eq(url),
                eq(HttpMethod.POST),
                any(HttpEntity::class.java),
                eq(Void::class.java),
            ),
        ).thenReturn(ResponseEntity.ok().build())

        authorizationServiceClient.revokePermission(requesterId, userId, snippetId)

        verify(restTemplate).exchange(
            eq(url),
            eq(HttpMethod.POST),
            any(HttpEntity::class.java),
            eq(Void::class.java),
        )
    }

    @Test
    fun `getSnippetPermissions should return list of permissions`() {
        val requesterId = "requester123"
        val snippetId = "1"
        val url = "$authorizationServiceUrl/api/authorization/permissions/snippet"
        val permissions =
            arrayOf(
                PermissionResponseDTO(
                    id = 1L,
                    snippetId = snippetId,
                    userId = "user1",
                    canRead = true,
                    canEdit = false,
                ),
                PermissionResponseDTO(
                    id = 2L,
                    snippetId = snippetId,
                    userId = "user2",
                    canRead = true,
                    canEdit = true,
                ),
            )

        authorizationServiceClient =
            AuthorizationServiceClient(restTemplate, authorizationServiceUrl)

        `when`(
            restTemplate.exchange(
                eq(url),
                eq(HttpMethod.POST),
                any(HttpEntity::class.java),
                eq(Array<PermissionResponseDTO>::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(permissions))

        val result = authorizationServiceClient.getSnippetPermissions(requesterId, snippetId)

        assertEquals(2, result.size)
        assertEquals("user1", result[0].userId)
        assertEquals("user2", result[1].userId)
    }

    @Test
    fun `getSnippetPermissions should return empty list when no permissions exist`() {
        val requesterId = "requester123"
        val snippetId = "1"
        val url = "$authorizationServiceUrl/api/authorization/permissions/snippet"

        authorizationServiceClient =
            AuthorizationServiceClient(restTemplate, authorizationServiceUrl)

        `when`(
            restTemplate.exchange(
                eq(url),
                eq(HttpMethod.POST),
                any(HttpEntity::class.java),
                eq(Array<PermissionResponseDTO>::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(emptyArray()))

        val result = authorizationServiceClient.getSnippetPermissions(requesterId, snippetId)

        assertEquals(0, result.size)
    }

    @Test
    fun `getSnippetPermissions should return empty list when response body is null`() {
        val requesterId = "requester123"
        val snippetId = "1"
        val url = "$authorizationServiceUrl/api/authorization/permissions/snippet"

        authorizationServiceClient =
            AuthorizationServiceClient(restTemplate, authorizationServiceUrl)

        `when`(
            restTemplate.exchange(
                eq(url),
                eq(HttpMethod.POST),
                any(HttpEntity::class.java),
                eq(Array<PermissionResponseDTO>::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(null))

        val result = authorizationServiceClient.getSnippetPermissions(requesterId, snippetId)

        assertEquals(0, result.size)
    }
}
