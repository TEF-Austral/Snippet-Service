package snippet.controllers

import common.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpStatus
import snippet.dtos.CreateSnippetDTO
import snippet.dtos.PaginatedSnippetsDTO
import snippet.dtos.SnippetResponseDTO
import snippet.dtos.UpdateSnippetDTO
import snippet.security.AuthenticatedUserProvider
import snippet.services.SnippetService
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class SnippetControllerTest {

    @Mock
    private lateinit var service: SnippetService

    @Mock
    private lateinit var authenticatedUserProvider: AuthenticatedUserProvider

    @InjectMocks
    private lateinit var controller: SnippetController

    @Test
    fun `createSnippet should return 201 with created snippet`() {
        val userId = "user123"
        val author = "John Doe"
        val createDTO =
            CreateSnippetDTO(
                content = "println('Hello')",
                name = "Test Snippet",
                description = "Test Description",
                language = Language.PRINTSCRIPT,
                version = "1.0",
            )
        val responseDTO =
            SnippetResponseDTO(
                snippetId = 1L,
                name = "Test Snippet",
                description = "Test Description",
                content = "println('Hello')",
                language = Language.PRINTSCRIPT,
                version = "1.0",
                author = author,
            )

        `when`(authenticatedUserProvider.getCurrentUserId()).thenReturn(userId)
        `when`(authenticatedUserProvider.getCurrentUserName()).thenReturn(author)
        `when`(service.createSnippet(createDTO, userId, author)).thenReturn(responseDTO)

        val response = controller.createSnippet(createDTO)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals(responseDTO, response.body)
        verify(service).createSnippet(createDTO, userId, author)
    }

    @Test
    fun `deleteSnippet should return 204 No Content`() {
        val snippetId = 1L
        val userId = "user123"

        `when`(authenticatedUserProvider.getCurrentUserId()).thenReturn(userId)

        val response = controller.deleteSnippet(snippetId)

        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
        verify(service).deleteSnippet(snippetId, userId)
    }

    @Test
    fun `updateSnippet should return 200 with updated snippet`() {
        val snippetId = 1L
        val userId = "user123"
        val updateDTO =
            UpdateSnippetDTO(
                content = "println('Updated')",
                name = "Updated Snippet",
                description = "Updated Description",
                language = Language.JAVA,
                version = "2.0",
            )
        val responseDTO =
            SnippetResponseDTO(
                snippetId = snippetId,
                name = "Updated Snippet",
                description = "Updated Description",
                content = "println('Updated')",
                language = Language.JAVA,
                version = "2.0",
                author = "John Doe",
            )

        `when`(authenticatedUserProvider.getCurrentUserId()).thenReturn(userId)
        `when`(service.updateSnippet(snippetId, updateDTO, userId)).thenReturn(responseDTO)

        val response = controller.updateSnippet(snippetId, updateDTO)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(responseDTO, response.body)
        verify(service).updateSnippet(snippetId, updateDTO, userId)
    }

    @Test
    fun `getSnippet should return 200 with snippet`() {
        val snippetId = 1L
        val userId = "user123"
        val responseDTO =
            SnippetResponseDTO(
                snippetId = snippetId,
                name = "Test Snippet",
                description = "Test Description",
                content = "println('Hello')",
                language = Language.PRINTSCRIPT,
                version = "1.0",
                author = "John Doe",
            )

        `when`(authenticatedUserProvider.getCurrentUserId()).thenReturn(userId)
        `when`(service.getSnippetById(snippetId, userId)).thenReturn(responseDTO)

        val response = controller.getSnippet(snippetId)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(responseDTO, response.body)
        verify(service).getSnippetById(snippetId, userId)
    }

    @Test
    fun `getMySnippets should return paginated snippets with default pagination`() {
        val userId = "user123"
        val paginatedDTO =
            PaginatedSnippetsDTO(
                page = 0,
                pageSize = 10,
                count = 5,
                snippets = listOf(),
            )

        `when`(authenticatedUserProvider.getCurrentUserId()).thenReturn(userId)
        `when`(service.getOwnerSnippets(userId, 0, 10)).thenReturn(paginatedDTO)

        val response = controller.getMySnippets(0, 10)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(paginatedDTO, response.body)
        verify(service).getOwnerSnippets(userId, 0, 10)
    }

    @Test
    fun `getMySnippets should handle custom pagination parameters`() {
        val userId = "user123"
        val page = 2
        val pageSize = 20
        val paginatedDTO =
            PaginatedSnippetsDTO(
                page = page,
                pageSize = pageSize,
                count = 100,
                snippets = listOf(),
            )

        `when`(authenticatedUserProvider.getCurrentUserId()).thenReturn(userId)
        `when`(service.getOwnerSnippets(userId, page, pageSize)).thenReturn(paginatedDTO)

        val response = controller.getMySnippets(page, pageSize)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(paginatedDTO, response.body)
    }
}
