package snippet.dtos

import common.Language
import org.junit.jupiter.api.Test
import snippet.dtos.requests.CreateSnippetRequestDTO
import snippet.dtos.requests.UpdateSnippetRequestDTO
import snippet.dtos.responses.CheckPermissionResponseDTO
import snippet.dtos.responses.FileTypeDTO
import snippet.dtos.responses.PermissionResponseDTO
import snippet.dtos.responses.ShareSnippetResponseDTO
import snippet.dtos.responses.SnippetResponseDTO
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SnippetDTOTest {

    @Test
    fun `CreateSnippetDTO should be created with all properties`() {
        val dto =
            CreateSnippetRequestDTO(
                content = "println('Hello')",
                name = "Test Snippet",
                description = "Test Description",
                language = Language.PRINTSCRIPT,
                version = "1.0",
            )

        assertEquals("println('Hello')", dto.content)
        assertEquals("Test Snippet", dto.name)
        assertEquals("Test Description", dto.description)
        assertEquals(Language.PRINTSCRIPT, dto.language)
        assertEquals("1.0", dto.version)
    }

    @Test
    fun `CreateSnippetDTO should handle empty content`() {
        val dto =
            CreateSnippetRequestDTO(
                content = "",
                name = "Empty Snippet",
                description = "Empty",
                language = Language.JAVA,
                version = "11",
            )

        assertEquals("", dto.content)
    }

    @Test
    fun `SnippetResponseDTO should be created with all properties`() {
        val dto =
            SnippetResponseDTO(
                snippetId = 1L,
                name = "Test Snippet",
                description = "Test Description",
                content = "println('Hello')",
                language = Language.PYTHON,
                version = "3.9",
                author = "John Doe",
                complianceStatus = "pending",
                validationErrors = null,
            )

        assertEquals(1L, dto.snippetId)
        assertEquals("Test Snippet", dto.name)
        assertEquals("Test Description", dto.description)
        assertEquals("println('Hello')", dto.content)
        assertEquals(Language.PYTHON, dto.language)
        assertEquals("3.9", dto.version)
        assertEquals("John Doe", dto.author)
    }

    @Test
    fun `UpdateSnippetDTO should handle partial updates`() {
        val dto =
            UpdateSnippetRequestDTO(
                content = "new content",
                name = "New Name",
                description = null,
                language = null,
                version = null,
            )

        assertEquals("new content", dto.content)
        assertEquals("New Name", dto.name)
        assertNull(dto.description)
        assertNull(dto.language)
        assertNull(dto.version)
    }

    @Test
    fun `UpdateSnippetDTO should handle all null values`() {
        val dto =
            UpdateSnippetRequestDTO(
                content = null,
                name = null,
                description = null,
                language = null,
                version = null,
            )

        assertNull(dto.content)
        assertNull(dto.name)
        assertNull(dto.description)
        assertNull(dto.language)
        assertNull(dto.version)
    }

    @Test
    fun `PaginatedSnippetsDTO should be created correctly`() {
        val snippets =
            listOf(
                SnippetResponseDTO(
                    snippetId = 1L,
                    name = "Snippet 1",
                    description = "Desc 1",
                    content = "content1",
                    language = Language.PRINTSCRIPT,
                    version = "1.0",
                    author = "Author 1",
                    complianceStatus = "pending",
                    validationErrors = null,
                ),
                SnippetResponseDTO(
                    snippetId = 2L,
                    name = "Snippet 2",
                    description = "Desc 2",
                    content = "content2",
                    language = Language.JAVA,
                    version = "11",
                    author = "Author 2",
                    complianceStatus = "pending",
                    validationErrors = null,
                ),
            )

        val dto =
            PaginatedSnippetsDTO(
                page = 0,
                pageSize = 10,
                count = 2,
                snippets = snippets,
            )

        assertEquals(0, dto.page)
        assertEquals(10, dto.pageSize)
        assertEquals(2, dto.count)
        assertEquals(2, dto.snippets.size)
    }

    @Test
    fun `ShareSnippetDTO should be created with permissions`() {
        val dto =
            ShareSnippetDTO(
                userId = "user123",
                canRead = true,
                canEdit = false,
            )

        assertEquals("user123", dto.userId)
        assertEquals(true, dto.canRead)
        assertEquals(false, dto.canEdit)
    }

    @Test
    fun `ShareSnippetResponseDTO should contain all fields`() {
        val permissions = PermissionsSummaryDTO(canRead = true, canEdit = true)
        val dto =
            ShareSnippetResponseDTO(
                message = "Snippet shared successfully",
                snippetId = 1L,
                sharedWith = "user123",
                permissions = permissions,
            )

        assertEquals("Snippet shared successfully", dto.message)
        assertEquals(1L, dto.snippetId)
        assertEquals("user123", dto.sharedWith)
        assertEquals(true, dto.permissions.canRead)
        assertEquals(true, dto.permissions.canEdit)
    }

    @Test
    fun `FileTypeDTO should be created correctly`() {
        val dto =
            FileTypeDTO(
                language = "PRINTSCRIPT",
                extension = "prs",
            )

        assertEquals("PRINTSCRIPT", dto.language)
        assertEquals("prs", dto.extension)
    }

    @Test
    fun `CheckPermissionResponseDTO should contain allowed field`() {
        val dto = CheckPermissionResponseDTO(allowed = true)
        assertEquals(true, dto.allowed)

        val dto2 = CheckPermissionResponseDTO(allowed = false)
        assertEquals(false, dto2.allowed)
    }

    @Test
    fun `PermissionResponseDTO should be created with all fields`() {
        val dto =
            PermissionResponseDTO(
                id = 1L,
                snippetId = "1",
                userId = "user123",
                canRead = true,
                canEdit = false,
            )

        assertEquals(1L, dto.id)
        assertEquals("1", dto.snippetId)
        assertEquals("user123", dto.userId)
        assertEquals(true, dto.canRead)
        assertEquals(false, dto.canEdit)
    }
}
