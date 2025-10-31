package snippet.services

import common.Language
import events.SnippetEventProducer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import snippet.component.AssetServiceClient
import snippet.component.AuthorizationServiceClient
import snippet.dtos.CreateSnippetDTO
import snippet.dtos.UpdateSnippetDTO
import snippet.entities.Snippet
import snippet.repositories.SnippetRepository
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.mockito.ArgumentMatchers.any

@ExtendWith(MockitoExtension::class)
class SnippetServiceImplTest {

    @Mock
    private lateinit var repository: SnippetRepository

    @Mock
    private lateinit var assetServiceClient: AssetServiceClient

    @Mock
    private lateinit var authorizationServiceClient: AuthorizationServiceClient

    @Mock
    private lateinit var eventProducer: SnippetEventProducer

    @InjectMocks
    private lateinit var service: SnippetServiceImpl

    @Test
    fun `createSnippet should create and save snippet successfully`() {
        val createDTO =
            CreateSnippetDTO(
                content = "println('Hello')",
                name = "Test Snippet",
                description = "Test Description",
                language = Language.PRINTSCRIPT,
                version = "1.0",
            )
        val ownerId = "owner123"
        val author = "John Doe"
        val savedSnippet =
            Snippet(
                id = 1L,
                name = "Test Snippet",
                description = "Test Description",
                ownerId = ownerId,
                bucketKey = "test-key",
                bucketContainer = "snippets",
                language = Language.PRINTSCRIPT,
                version = "1.0",
                author = author,
            )

        `when`(repository.save(any(Snippet::class.java))).thenReturn(savedSnippet)
        `when`(assetServiceClient.getAsset("snippets", "test-key")).thenReturn("println('Hello')")

        val result = service.createSnippet(createDTO, ownerId, author)

        assertNotNull(result)
        assertEquals(1L, result.snippetId)
        assertEquals("Test Snippet", result.name)
        assertEquals("Test Description", result.description)
        verify(assetServiceClient).createOrUpdateAsset("snippets", "test-key", "println('Hello')")
    }

    @Test
    fun `createSnippet should handle empty content`() {
        val createDTO =
            CreateSnippetDTO(
                content = "",
                name = "Empty Snippet",
                description = "Empty",
                language = Language.JAVA,
                version = "11",
            )
        val ownerId = "owner123"
        val author = "Jane Doe"
        val savedSnippet =
            Snippet(
                id = 2L,
                name = "Empty Snippet",
                description = "Empty",
                ownerId = ownerId,
                bucketKey = "test-key-2",
                bucketContainer = "snippets",
                language = Language.JAVA,
                version = "11",
                author = author,
            )

        `when`(repository.save(any(Snippet::class.java))).thenReturn(savedSnippet)
        `when`(assetServiceClient.getAsset("snippets", "test-key-2")).thenReturn("")

        val result = service.createSnippet(createDTO, ownerId, author)

        assertNotNull(result)
        verify(assetServiceClient).createOrUpdateAsset("snippets", "test-key-2", "")
    }

    @Test
    fun `getSnippetById should return snippet when user has permission`() {
        val snippetId = 1L
        val requesterId = "user123"
        val snippet =
            Snippet(
                id = snippetId,
                name = "Test",
                description = "Test",
                ownerId = "owner123",
                bucketKey = "test-key",
                language = Language.PYTHON,
                version = "3.9",
                author = "Author",
            )

        `when`(repository.findById(snippetId)).thenReturn(Optional.of(snippet))
        `when`(
            authorizationServiceClient.checkPermission(
                requesterId,
                "read",
                snippetId.toString(),
                "owner123",
            ),
        ).thenReturn(true)
        `when`(assetServiceClient.getAsset("snippets", "test-key")).thenReturn("print('hello')")

        val result = service.getSnippetById(snippetId, requesterId)

        assertNotNull(result)
        assertEquals(snippetId, result.snippetId)
        assertEquals("Test", result.name)
    }

    @Test
    fun `getSnippetById should throw exception when snippet not found`() {
        val snippetId = 1L
        val requesterId = "user123"

        `when`(repository.findById(snippetId)).thenReturn(Optional.empty())

        try {
            service.getSnippetById(snippetId, requesterId)
            throw AssertionError("Expected NoSuchElementException")
        } catch (e: NoSuchElementException) {
            assertEquals("Snippet not found: $snippetId", e.message)
        }
    }

    @Test
    fun `getSnippetById should throw exception when user has no permission`() {
        val snippetId = 1L
        val requesterId = "user123"
        val snippet =
            Snippet(
                id = snippetId,
                name = "Test",
                description = "Test",
                ownerId = "owner123",
                bucketKey = "test-key",
                language = Language.GOLANG,
                version = "1.18",
                author = "Author",
            )

        `when`(repository.findById(snippetId)).thenReturn(Optional.of(snippet))
        `when`(
            authorizationServiceClient.checkPermission(
                requesterId,
                "read",
                snippetId.toString(),
                "owner123",
            ),
        ).thenReturn(false)

        try {
            service.getSnippetById(snippetId, requesterId)
            throw AssertionError("Expected IllegalAccessException")
        } catch (e: IllegalAccessException) {
            assertEquals("You don't have permission to access this snippet", e.message)
        }
    }

    @Test
    fun `updateSnippet should update snippet when user has permission`() {
        val snippetId = 1L
        val requesterId = "user123"
        val existing =
            Snippet(
                id = snippetId,
                name = "Old Name",
                description = "Old Desc",
                ownerId = "owner123",
                bucketKey = "test-key",
                language = Language.PRINTSCRIPT,
                version = "1.0",
                author = "Author",
            )
        val updateDTO =
            UpdateSnippetDTO(
                name = "New Name",
                description = "New Desc",
                content = "new content",
                language = Language.JAVA,
                version = "11",
            )

        `when`(repository.findById(snippetId)).thenReturn(Optional.of(existing))
        `when`(
            authorizationServiceClient.checkPermission(
                requesterId,
                "edit",
                snippetId.toString(),
                "owner123",
            ),
        ).thenReturn(true)
        `when`(repository.save(any(Snippet::class.java))).thenReturn(existing)
        `when`(assetServiceClient.getAsset("snippets", "test-key")).thenReturn("new content")

        val result = service.updateSnippet(snippetId, updateDTO, requesterId)

        assertEquals("New Name", existing.name)
        assertEquals("New Desc", existing.description)
        assertEquals(Language.JAVA, existing.language)
        verify(assetServiceClient).createOrUpdateAsset("snippets", "test-key", "new content")
    }

    @Test
    fun `updateSnippet should update only provided fields`() {
        val snippetId = 1L
        val requesterId = "user123"
        val existing =
            Snippet(
                id = snippetId,
                name = "Old Name",
                description = "Old Desc",
                ownerId = "owner123",
                bucketKey = "test-key",
                language = Language.PRINTSCRIPT,
                version = "1.0",
                author = "Author",
            )
        val updateDTO =
            UpdateSnippetDTO(
                name = "New Name",
                description = null,
                content = null,
                language = null,
                version = null,
            )

        `when`(repository.findById(snippetId)).thenReturn(Optional.of(existing))
        `when`(
            authorizationServiceClient.checkPermission(
                requesterId,
                "edit",
                snippetId.toString(),
                "owner123",
            ),
        ).thenReturn(true)
        `when`(repository.save(any(Snippet::class.java))).thenReturn(existing)
        `when`(assetServiceClient.getAsset("snippets", "test-key")).thenReturn("content")

        val result = service.updateSnippet(snippetId, updateDTO, requesterId)

        assertEquals("New Name", existing.name)
        assertEquals("Old Desc", existing.description)
        assertEquals(Language.PRINTSCRIPT, existing.language)
    }

    @Test
    fun `updateSnippet should throw exception when snippet not found`() {
        val snippetId = 1L
        val requesterId = "user123"
        val updateDTO =
            UpdateSnippetDTO(
                name = "New",
                description = null,
                content = null,
                language = null,
                version = null,
            )

        `when`(repository.findById(snippetId)).thenReturn(Optional.empty())

        try {
            service.updateSnippet(snippetId, updateDTO, requesterId)
            throw AssertionError("Expected NoSuchElementException")
        } catch (e: NoSuchElementException) {
            assertEquals("Snippet not found: $snippetId", e.message)
        }
    }

    @Test
    fun `updateSnippet should throw exception when user has no permission`() {
        val snippetId = 1L
        val requesterId = "user123"
        val existing =
            Snippet(
                id = snippetId,
                name = "Test",
                description = "Test",
                ownerId = "owner123",
                bucketKey = "test-key",
                language = Language.PYTHON,
                version = "3.9",
                author = "Author",
            )
        val updateDTO =
            UpdateSnippetDTO(
                name = "New",
                description = null,
                content = null,
                language = null,
                version = null,
            )

        `when`(repository.findById(snippetId)).thenReturn(Optional.of(existing))
        `when`(
            authorizationServiceClient.checkPermission(
                requesterId,
                "edit",
                snippetId.toString(),
                "owner123",
            ),
        ).thenReturn(false)

        try {
            service.updateSnippet(snippetId, updateDTO, requesterId)
            throw AssertionError("Expected IllegalAccessException")
        } catch (e: IllegalAccessException) {
            assertEquals("You don't have permission to update this snippet", e.message)
        }
    }

    @Test
    fun `deleteSnippet should delete snippet when user has permission`() {
        val snippetId = 1L
        val requesterId = "user123"
        val snippet =
            Snippet(
                id = snippetId,
                name = "Test",
                description = "Test",
                ownerId = "owner123",
                bucketKey = "test-key",
                language = Language.GOLANG,
                version = "1.18",
                author = "Author",
            )

        `when`(repository.findById(snippetId)).thenReturn(Optional.of(snippet))
        `when`(
            authorizationServiceClient.checkPermission(
                requesterId,
                "delete",
                snippetId.toString(),
                "owner123",
            ),
        ).thenReturn(true)

        service.deleteSnippet(snippetId, requesterId)

        verify(assetServiceClient).deleteAsset("snippets", "test-key")
        verify(repository).deleteById(snippetId)
    }

    @Test
    fun `deleteSnippet should throw exception when snippet not found`() {
        val snippetId = 1L
        val requesterId = "user123"

        `when`(repository.findById(snippetId)).thenReturn(Optional.empty())

        try {
            service.deleteSnippet(snippetId, requesterId)
            throw AssertionError("Expected NoSuchElementException")
        } catch (e: NoSuchElementException) {
            assertEquals("Snippet not found: $snippetId", e.message)
        }
    }
}
