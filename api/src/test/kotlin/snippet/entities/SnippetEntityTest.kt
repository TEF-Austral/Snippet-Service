package snippet.entities

import common.Language
import common.entities.Snippet
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SnippetEntityTest {

    @Test
    fun `Snippet should be created with all properties`() {
        val snippet =
            Snippet(
                id = 1L,
                name = "Test Snippet",
                description = "Test Description",
                ownerId = "owner123",
                bucketKey = "test-key",
                bucketContainer = "snippets",
                language = Language.PRINTSCRIPT,
                version = "1.0",
                author = "John Doe",
            )

        assertEquals(1L, snippet.id)
        assertEquals("Test Snippet", snippet.name)
        assertEquals("Test Description", snippet.description)
        assertEquals("owner123", snippet.ownerId)
        assertEquals("test-key", snippet.bucketKey)
        assertEquals("snippets", snippet.bucketContainer)
        assertEquals(Language.PRINTSCRIPT, snippet.language)
        assertEquals("1.0", snippet.version)
        assertEquals("John Doe", snippet.author)
    }

    @Test
    fun `Snippet should allow null id for new entities`() {
        val snippet =
            Snippet(
                id = null,
                name = "New Snippet",
                description = "New Description",
                ownerId = "owner123",
                bucketKey = null,
                language = Language.JAVA,
                version = "11",
                author = "Jane Doe",
            )

        assertNull(snippet.id)
        assertEquals("New Snippet", snippet.name)
    }

    @Test
    fun `ensureBucketKey should generate bucket key when null`() {
        val snippet =
            Snippet(
                id = null,
                name = "Test",
                description = "Test",
                ownerId = "owner123",
                bucketKey = null,
                language = Language.PYTHON,
                version = "3.9",
                author = "Author",
            )

        assertNull(snippet.bucketKey)

        snippet.ensureBucketKey()

        assertNotNull(snippet.bucketKey)
    }

    @Test
    fun `ensureBucketKey should not overwrite existing bucket key`() {
        val existingKey = "existing-key"
        val snippet =
            Snippet(
                id = 1L,
                name = "Test",
                description = "Test",
                ownerId = "owner123",
                bucketKey = existingKey,
                language = Language.GOLANG,
                version = "1.18",
                author = "Author",
            )

        snippet.ensureBucketKey()

        assertEquals(existingKey, snippet.bucketKey)
    }

    @Test
    fun `ensureBucketKey should handle empty string as blank`() {
        val snippet =
            Snippet(
                id = null,
                name = "Test",
                description = "Test",
                ownerId = "owner123",
                bucketKey = "",
                language = Language.JAVA,
                version = "11",
                author = "Author",
            )

        snippet.ensureBucketKey()

        assertNotNull(snippet.bucketKey)
        assert(snippet.bucketKey!!.isNotEmpty())
    }

    @Test
    fun `Snippet should support all languages`() {
        val printscriptSnippet =
            Snippet(
                id = 1L,
                name = "PS",
                description = "desc",
                ownerId = "owner",
                bucketKey = "key1",
                language = Language.PRINTSCRIPT,
                version = "1.0",
                author = "Author",
            )
        assertEquals(Language.PRINTSCRIPT, printscriptSnippet.language)

        val javaSnippet =
            Snippet(
                id = 2L,
                name = "Java",
                description = "desc",
                ownerId = "owner",
                bucketKey = "key2",
                language = Language.JAVA,
                version = "11",
                author = "Author",
            )
        assertEquals(Language.JAVA, javaSnippet.language)

        val pythonSnippet =
            Snippet(
                id = 3L,
                name = "Python",
                description = "desc",
                ownerId = "owner",
                bucketKey = "key3",
                language = Language.PYTHON,
                version = "3.9",
                author = "Author",
            )
        assertEquals(Language.PYTHON, pythonSnippet.language)

        val golangSnippet =
            Snippet(
                id = 4L,
                name = "Go",
                description = "desc",
                ownerId = "owner",
                bucketKey = "key4",
                language = Language.GOLANG,
                version = "1.18",
                author = "Author",
            )
        assertEquals(Language.GOLANG, golangSnippet.language)
    }

    @Test
    fun `Snippet properties should be mutable`() {
        val snippet =
            Snippet(
                id = 1L,
                name = "Original",
                description = "Original Description",
                ownerId = "owner123",
                bucketKey = "key",
                language = Language.PRINTSCRIPT,
                version = "1.0",
                author = "Original Author",
            )

        snippet.name = "Updated"
        snippet.description = "Updated Description"
        snippet.language = Language.JAVA
        snippet.version = "2.0"
        snippet.author = "Updated Author"

        assertEquals("Updated", snippet.name)
        assertEquals("Updated Description", snippet.description)
        assertEquals(Language.JAVA, snippet.language)
        assertEquals("2.0", snippet.version)
        assertEquals("Updated Author", snippet.author)
    }

    @Test
    fun `Snippet should have default bucket container`() {
        val snippet =
            Snippet(
                id = 1L,
                name = "Test",
                description = "Test",
                ownerId = "owner123",
                language = Language.PRINTSCRIPT,
                version = "1.0",
                author = "Author",
            )

        assertEquals("snippets", snippet.bucketContainer)
    }
}
