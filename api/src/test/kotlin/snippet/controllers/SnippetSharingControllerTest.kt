package snippet.controllers

import authorization.AuthorizationServiceClient
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpStatus
import common.entities.Snippet
import common.repositories.SnippetRepository
import common.Language
import common.dtos.requests.ShareSnippetDTO
import security.AuthenticatedUserProvider
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExtendWith(MockitoExtension::class)
class SnippetSharingControllerTest {

    @Mock
    private lateinit var snippetRepository: SnippetRepository

    @Mock
    private lateinit var authorizationServiceClient: AuthorizationServiceClient

    @Mock
    private lateinit var authenticatedUserProvider: AuthenticatedUserProvider

    @InjectMocks
    private lateinit var controller: SnippetSharingController

    @Test
    fun `shareSnippet should share snippet successfully with read and edit permissions`() {
        val snippetId = 1L
        val userId = "user123"
        val ownerId = "owner456"
        val granteeId = "grantee789"
        val snippet =
            Snippet(
                id = snippetId,
                name = "Test",
                description = "Test",
                ownerId = ownerId,
                bucketKey = "test-key",
                language = Language.PRINTSCRIPT,
                version = "1.0",
                author = "Test Author",
            )
        val shareDTO =
            ShareSnippetDTO(
                userId = granteeId,
                canRead = true,
                canEdit = true,
            )

        `when`(authenticatedUserProvider.getCurrentUserId()).thenReturn(userId)
        `when`(snippetRepository.findById(snippetId)).thenReturn(Optional.of(snippet))
        `when`(
            authorizationServiceClient.checkPermission(
                userId,
                "share",
                snippetId.toString(),
                ownerId,
            ),
        ).thenReturn(true)

        val response = controller.shareSnippet(snippetId, shareDTO)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals("Snippet shared successfully", response.body?.message)
        assertEquals(snippetId, response.body?.snippetId)
        assertEquals(granteeId, response.body?.sharedWith)
        assertEquals(true, response.body?.permissions?.canRead)
        assertEquals(true, response.body?.permissions?.canEdit)
        verify(
            authorizationServiceClient,
        ).grantPermission(userId, ownerId, granteeId, snippetId.toString(), true, true)
    }

    @Test
    fun `shareSnippet should share with read-only permission`() {
        val snippetId = 1L
        val userId = "user123"
        val ownerId = "owner456"
        val granteeId = "grantee789"
        val snippet =
            Snippet(
                id = snippetId,
                name = "Test",
                description = "Test",
                ownerId = ownerId,
                bucketKey = "test-key",
                language = Language.JAVA,
                version = "1.0",
                author = "Test Author",
            )
        val shareDTO =
            ShareSnippetDTO(
                userId = granteeId,
                canRead = true,
                canEdit = false,
            )

        `when`(authenticatedUserProvider.getCurrentUserId()).thenReturn(userId)
        `when`(snippetRepository.findById(snippetId)).thenReturn(Optional.of(snippet))
        `when`(
            authorizationServiceClient.checkPermission(
                userId,
                "share",
                snippetId.toString(),
                ownerId,
            ),
        ).thenReturn(true)

        val response = controller.shareSnippet(snippetId, shareDTO)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(true, response.body?.permissions?.canRead)
        assertEquals(false, response.body?.permissions?.canEdit)
    }

    @Test
    fun `shareSnippet should throw exception when snippet not found`() {
        val snippetId = 1L
        val userId = "user123"
        val shareDTO =
            ShareSnippetDTO(
                userId = "grantee789",
                canRead = true,
                canEdit = true,
            )

        `when`(authenticatedUserProvider.getCurrentUserId()).thenReturn(userId)
        `when`(snippetRepository.findById(snippetId)).thenReturn(Optional.empty())

        try {
            controller.shareSnippet(snippetId, shareDTO)
            throw AssertionError("Expected NoSuchElementException")
        } catch (e: NoSuchElementException) {
            assertEquals("Snippet not found: $snippetId", e.message)
        }
    }

    @Test
    fun `shareSnippet should throw exception when user has no permission to share`() {
        val snippetId = 1L
        val userId = "user123"
        val ownerId = "owner456"
        val snippet =
            Snippet(
                id = snippetId,
                name = "Test",
                description = "Test",
                ownerId = ownerId,
                bucketKey = "test-key",
                language = Language.PYTHON,
                version = "1.0",
                author = "Test Author",
            )
        val shareDTO =
            ShareSnippetDTO(
                userId = "grantee789",
                canRead = true,
                canEdit = false,
            )

        `when`(authenticatedUserProvider.getCurrentUserId()).thenReturn(userId)
        `when`(snippetRepository.findById(snippetId)).thenReturn(Optional.of(snippet))
        `when`(
            authorizationServiceClient.checkPermission(
                userId,
                "share",
                snippetId.toString(),
                ownerId,
            ),
        ).thenReturn(false)

        try {
            controller.shareSnippet(snippetId, shareDTO)
            throw AssertionError("Expected IllegalAccessException")
        } catch (e: IllegalAccessException) {
            assertEquals("You don't have permission to share this snippet", e.message)
        }
    }

    @Test
    fun `revokeAccess should revoke access successfully`() {
        val snippetId = 1L
        val requesterId = "requester123"
        val targetUserId = "target456"
        val ownerId = "owner789"
        val snippet =
            Snippet(
                id = snippetId,
                name = "Test",
                description = "Test",
                ownerId = ownerId,
                bucketKey = "test-key",
                language = Language.GOLANG,
                version = "1.0",
                author = "Test Author",
            )

        `when`(authenticatedUserProvider.getCurrentUserId()).thenReturn(requesterId)
        `when`(snippetRepository.findById(snippetId)).thenReturn(Optional.of(snippet))
        `when`(
            authorizationServiceClient.checkPermission(
                requesterId,
                "share",
                snippetId.toString(),
                ownerId,
            ),
        ).thenReturn(true)

        val response = controller.revokeAccess(snippetId, targetUserId)

        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
        verify(
            authorizationServiceClient,
        ).revokePermission(requesterId, targetUserId, snippetId.toString())
    }

    @Test
    fun `revokeAccess should throw exception when snippet not found`() {
        val snippetId = 1L
        val requesterId = "requester123"
        val targetUserId = "target456"

        `when`(authenticatedUserProvider.getCurrentUserId()).thenReturn(requesterId)
        `when`(snippetRepository.findById(snippetId)).thenReturn(Optional.empty())

        try {
            controller.revokeAccess(snippetId, targetUserId)
            throw AssertionError("Expected NoSuchElementException")
        } catch (e: NoSuchElementException) {
            assertEquals("Snippet not found: $snippetId", e.message)
        }
    }

    @Test
    fun `revokeAccess should throw exception when user has no permission`() {
        val snippetId = 1L
        val requesterId = "requester123"
        val targetUserId = "target456"
        val ownerId = "owner789"
        val snippet =
            Snippet(
                id = snippetId,
                name = "Test",
                description = "Test",
                ownerId = ownerId,
                bucketKey = "test-key",
                language = Language.PRINTSCRIPT,
                version = "1.0",
                author = "Test Author",
            )

        `when`(authenticatedUserProvider.getCurrentUserId()).thenReturn(requesterId)
        `when`(snippetRepository.findById(snippetId)).thenReturn(Optional.of(snippet))
        `when`(
            authorizationServiceClient.checkPermission(
                requesterId,
                "share",
                snippetId.toString(),
                ownerId,
            ),
        ).thenReturn(false)

        try {
            controller.revokeAccess(snippetId, targetUserId)
            throw AssertionError("Expected IllegalAccessException")
        } catch (e: IllegalAccessException) {
            assertEquals("You don't have permission to revoke access", e.message)
        }
    }
}
