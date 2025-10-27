package services

import clients.AssetServiceClient
import dtos.SnippetRequestDTO
import dtos.UpdateSnippetDTO
import entities.Language
import entities.Snippet
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import repositories.SnippetRepository
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SnippetServiceImplTest {

    @Test
    fun `creates snippet with valid data and returns response`() {
        val repository = FakeSnippetRepository()
        val assetClient = FakeAssetServiceClient()
        val service = SnippetServiceImpl(repository, assetClient)

        val request =
            SnippetRequestDTO(
                name = "Test Snippet",
                description = "A test snippet",
                language = Language.PRINTSCRIPT,
                version = "1.0",
            )

        val result = service.createSnippet(request)

        assertEquals("Test Snippet", result.name)
        assertEquals("A test snippet", result.description)
        assertEquals(Language.PRINTSCRIPT, result.language)
        assertEquals("1.0", result.version)
        assertEquals("snippets", result.bucketContainer)
        assertNotNull(result.bucketKey)
    }

    @Test
    fun `creates snippet with generated bucket key when not provided`() {
        val repository = FakeSnippetRepository()
        val assetClient = FakeAssetServiceClient()
        val service = SnippetServiceImpl(repository, assetClient)

        val request =
            SnippetRequestDTO(
                name = "Snippet",
                description = "Description",
                language = Language.PRINTSCRIPT,
                version = "1.0",
            )

        val result = service.createSnippet(request)

        assertEquals("default-bucket-key", result.bucketKey)
    }

    @Test
    fun `retrieves snippet by id when it exists`() {
        val repository = FakeSnippetRepository()
        val assetClient = FakeAssetServiceClient()
        val service = SnippetServiceImpl(repository, assetClient)

        val snippet =
            Snippet(
                id = 1L,
                name = "Existing Snippet",
                description = "Description",
                bucketKey = "key-123",
                bucketContainer = "snippets",
                language = Language.PRINTSCRIPT,
                version = "1.0",
            )
        repository.saveSnippet(snippet)

        val result = service.getSnippetById(1L)

        assertEquals(1L, result.snippetId)
        assertEquals("Existing Snippet", result.name)
        assertEquals("key-123", result.bucketKey)
    }

    @Test
    fun `throws exception when retrieving non-existent snippet by id`() {
        val repository = FakeSnippetRepository()
        val assetClient = FakeAssetServiceClient()
        val service = SnippetServiceImpl(repository, assetClient)

        assertThrows<NoSuchElementException> {
            service.getSnippetById(999L)
        }
    }

    @Test
    fun `retrieves snippets by bucket id`() {
        val repository = FakeSnippetRepository()
        val assetClient = FakeAssetServiceClient()
        val service = SnippetServiceImpl(repository, assetClient)

        repository.saveSnippet(
            Snippet(1L, "Snippet1", "Desc1", "bucket-1", "snippets", Language.PRINTSCRIPT, "1.0"),
        )
        repository.saveSnippet(
            Snippet(2L, "Snippet2", "Desc2", "bucket-1", "snippets", Language.PRINTSCRIPT, "1.0"),
        )
        repository.saveSnippet(
            Snippet(3L, "Snippet3", "Desc3", "bucket-2", "snippets", Language.PRINTSCRIPT, "1.0"),
        )

        val results = service.getSnippetsByBucketId("bucket-1")

        assertEquals(2, results.size)
    }

    @Test
    fun `retrieves empty list when no snippets match bucket id`() {
        val repository = FakeSnippetRepository()
        val assetClient = FakeAssetServiceClient()
        val service = SnippetServiceImpl(repository, assetClient)

        val results = service.getSnippetsByBucketId("non-existent")

        assertEquals(0, results.size)
    }

    @Test
    fun `retrieves snippets by owner id`() {
        val repository = FakeSnippetRepository()
        val assetClient = FakeAssetServiceClient()
        val service = SnippetServiceImpl(repository, assetClient)

        repository.saveSnippetWithOwner(1L, "owner-1")
        repository.saveSnippetWithOwner(2L, "owner-1")
        repository.saveSnippetWithOwner(3L, "owner-2")

        val results = service.getOwnerSnippets("owner-1")

        assertEquals(2, results.size)
    }

    @Test
    fun `retrieves empty list when no snippets match owner id`() {
        val repository = FakeSnippetRepository()
        val assetClient = FakeAssetServiceClient()
        val service = SnippetServiceImpl(repository, assetClient)

        val results = service.getOwnerSnippets("unknown-owner")

        assertEquals(0, results.size)
    }

    @Test
    fun `updates snippet name when provided`() {
        val repository = FakeSnippetRepository()
        val assetClient = FakeAssetServiceClient()
        val service = SnippetServiceImpl(repository, assetClient)

        val snippet =
            Snippet(
                id = 1L,
                name = "Old Name",
                description = "Description",
                bucketKey = "key-123",
                bucketContainer = "snippets",
                language = Language.PRINTSCRIPT,
                version = "1.0",
            )
        repository.saveSnippet(snippet)

        val update =
            UpdateSnippetDTO(
                name = "New Name",
                content = null,
                description = null,
                language = null,
                version = null,
            )

        val result = service.updateSnippet(1L, update)

        assertEquals("New Name", result.name)
        assertEquals("Description", result.description)
    }

    @Test
    fun `updates snippet description when provided`() {
        val repository = FakeSnippetRepository()
        val assetClient = FakeAssetServiceClient()
        val service = SnippetServiceImpl(repository, assetClient)

        val snippet =
            Snippet(
                id = 1L,
                name = "Name",
                description = "Old Description",
                bucketKey = "key-123",
                bucketContainer = "snippets",
                language = Language.PRINTSCRIPT,
                version = "1.0",
            )
        repository.saveSnippet(snippet)

        val update =
            UpdateSnippetDTO(
                name = null,
                content = null,
                description = "New Description",
                language = null,
                version = null,
            )

        val result = service.updateSnippet(1L, update)

        assertEquals("New Description", result.description)
    }

    @Test
    fun `updates snippet content through asset service when provided`() {
        val repository = FakeSnippetRepository()
        val assetClient = FakeAssetServiceClient()
        val service = SnippetServiceImpl(repository, assetClient)

        val snippet =
            Snippet(
                id = 1L,
                name = "Name",
                description = "Description",
                bucketKey = "key-123",
                bucketContainer = "snippets",
                language = Language.PRINTSCRIPT,
                version = "1.0",
            )
        repository.saveSnippet(snippet)

        val update =
            UpdateSnippetDTO(
                name = null,
                content = "new content",
                description = null,
                language = null,
                version = null,
            )

        service.updateSnippet(1L, update)

        assertEquals(1, assetClient.createOrUpdateCalls.size)
        assertEquals("snippets", assetClient.createOrUpdateCalls[0].container)
        assertEquals("key-123", assetClient.createOrUpdateCalls[0].key)
        assertEquals("new content", assetClient.createOrUpdateCalls[0].content)
    }

    @Test
    fun `updates snippet language when provided`() {
        val repository = FakeSnippetRepository()
        val assetClient = FakeAssetServiceClient()
        val service = SnippetServiceImpl(repository, assetClient)

        val snippet =
            Snippet(
                id = 1L,
                name = "Name",
                description = "Description",
                bucketKey = "key-123",
                bucketContainer = "snippets",
                language = Language.PRINTSCRIPT,
                version = "1.0",
            )
        repository.saveSnippet(snippet)

        val update =
            UpdateSnippetDTO(
                name = null,
                content = null,
                description = null,
                language = Language.PRINTSCRIPT,
                version = null,
            )

        val result = service.updateSnippet(1L, update)

        assertEquals(Language.PRINTSCRIPT, result.language)
    }

    @Test
    fun `updates snippet version when provided`() {
        val repository = FakeSnippetRepository()
        val assetClient = FakeAssetServiceClient()
        val service = SnippetServiceImpl(repository, assetClient)

        val snippet =
            Snippet(
                id = 1L,
                name = "Name",
                description = "Description",
                bucketKey = "key-123",
                bucketContainer = "snippets",
                language = Language.PRINTSCRIPT,
                version = "1.0",
            )
        repository.saveSnippet(snippet)

        val update =
            UpdateSnippetDTO(
                name = null,
                content = null,
                description = null,
                language = null,
                version = "2.0",
            )

        val result = service.updateSnippet(1L, update)

        assertEquals("2.0", result.version)
    }

    @Test
    fun `updates multiple fields simultaneously`() {
        val repository = FakeSnippetRepository()
        val assetClient = FakeAssetServiceClient()
        val service = SnippetServiceImpl(repository, assetClient)

        val snippet =
            Snippet(
                id = 1L,
                name = "Old Name",
                description = "Old Description",
                bucketKey = "key-123",
                bucketContainer = "snippets",
                language = Language.PRINTSCRIPT,
                version = "1.0",
            )
        repository.saveSnippet(snippet)

        val update =
            UpdateSnippetDTO(
                name = "New Name",
                content = "New Content",
                description = "New Description",
                language = Language.PRINTSCRIPT,
                version = "2.0",
            )

        val result = service.updateSnippet(1L, update)

        assertEquals("New Name", result.name)
        assertEquals("New Description", result.description)
        assertEquals("2.0", result.version)
        assertEquals(1, assetClient.createOrUpdateCalls.size)
    }

    @Test
    fun `throws exception when updating non-existent snippet`() {
        val repository = FakeSnippetRepository()
        val assetClient = FakeAssetServiceClient()
        val service = SnippetServiceImpl(repository, assetClient)

        val update =
            UpdateSnippetDTO(
                name = "Name",
                content = null,
                description = null,
                language = null,
                version = null,
            )

        assertThrows<NoSuchElementException> {
            service.updateSnippet(999L, update)
        }
    }

    @Test
    fun `throws exception when updating snippet content with null bucket key`() {
        val repository = FakeSnippetRepository()
        val assetClient = FakeAssetServiceClient()
        val service = SnippetServiceImpl(repository, assetClient)

        val snippet =
            Snippet(
                id = 1L,
                name = "Name",
                description = "Description",
                bucketKey = null,
                bucketContainer = "snippets",
                language = Language.PRINTSCRIPT,
                version = "1.0",
            )
        repository.saveSnippet(snippet)

        val update =
            UpdateSnippetDTO(
                name = null,
                content = "content",
                description = null,
                language = null,
                version = null,
            )

        assertThrows<IllegalStateException> {
            service.updateSnippet(1L, update)
        }
    }

    @Test
    fun `deletes snippet and associated asset when snippet exists`() {
        val repository = FakeSnippetRepository()
        val assetClient = FakeAssetServiceClient()
        val service = SnippetServiceImpl(repository, assetClient)

        val snippet =
            Snippet(
                id = 1L,
                name = "Name",
                description = "Description",
                bucketKey = "key-123",
                bucketContainer = "snippets",
                language = Language.PRINTSCRIPT,
                version = "1.0",
            )
        repository.saveSnippet(snippet)

        service.deleteSnippet(1L)

        assertEquals(1, assetClient.deleteCalls.size)
        assertEquals("snippets", assetClient.deleteCalls[0].container)
        assertEquals("key-123", assetClient.deleteCalls[0].key)
        assertEquals(false, repository.existsById(1L))
    }

    @Test
    fun `throws exception when deleting non-existent snippet`() {
        val repository = FakeSnippetRepository()
        val assetClient = FakeAssetServiceClient()
        val service = SnippetServiceImpl(repository, assetClient)

        assertThrows<NoSuchElementException> {
            service.deleteSnippet(999L)
        }
    }

    @Test
    fun `throws exception when deleting snippet with null bucket key`() {
        val repository = FakeSnippetRepository()
        val assetClient = FakeAssetServiceClient()
        val service = SnippetServiceImpl(repository, assetClient)

        val snippet =
            Snippet(
                id = 1L,
                name = "Name",
                description = "Description",
                bucketKey = null,
                bucketContainer = "snippets",
                language = Language.PRINTSCRIPT,
                version = "1.0",
            )
        repository.saveSnippet(snippet)

        assertThrows<IllegalStateException> {
            service.deleteSnippet(1L)
        }
    }
}

class FakeSnippetRepository : SnippetRepository {
    private val snippets = mutableMapOf<Long, Snippet>()
    private val ownerMap = mutableMapOf<Long, String>()
    private var nextId = 1L

    fun saveSnippet(snippet: Snippet) {
        val id = snippet.id ?: nextId++
        snippets[id] = snippet.copy(id = id)
    }

    fun saveSnippetWithOwner(
        snippetId: Long,
        ownerId: String,
    ) {
        snippets[snippetId] =
            Snippet(snippetId, "Name", "Desc", "key", "snippets", Language.PRINTSCRIPT, "1.0")
        ownerMap[snippetId] = ownerId
    }

    override fun <S : Snippet> save(entity: S): S {
        val id = entity.id ?: nextId++

        @Suppress("UNCHECKED_CAST")
        val saved = entity.copy(id = id, bucketKey = entity.bucketKey ?: "default-bucket-key") as S
        snippets[id] = saved
        return saved
    }

    override fun findById(id: Long): Optional<Snippet> = Optional.ofNullable(snippets[id])

    override fun existsById(id: Long): Boolean = snippets.containsKey(id)

    override fun findByBucketId(bucketId: String): List<Snippet> =
        snippets.values.filter { it.bucketKey == bucketId }

    override fun findByOwnerId(ownerId: String): List<Snippet> =
        ownerMap.filter { it.value == ownerId }.mapNotNull { snippets[it.key] }

    override fun deleteById(id: Long) {
        snippets.remove(id)
        ownerMap.remove(id)
    }

    override fun findAll(): List<Snippet> = snippets.values.toList()

    override fun findAllById(ids: Iterable<Long>): List<Snippet> = ids.mapNotNull { snippets[it] }

    override fun count(): Long = snippets.size.toLong()

    override fun delete(entity: Snippet) {
        entity.id?.let { snippets.remove(it) }
    }

    override fun deleteAllById(ids: Iterable<Long>) {
        ids.forEach { id -> snippets.remove(id) }
    }

    override fun deleteAll(entities: Iterable<Snippet>) {
        entities.forEach { it.id?.let { id -> snippets.remove(id) } }
    }

    override fun deleteAll() {
        snippets.clear()
    }

    override fun <S : Snippet> saveAll(entities: Iterable<S>): List<S> = entities.map { save(it) }

    override fun flush() {}

    override fun <S : Snippet> saveAndFlush(entity: S): S = save(entity)

    override fun <S : Snippet> saveAllAndFlush(entities: Iterable<S>): List<S> = saveAll(entities)

    override fun deleteAllInBatch(entities: Iterable<Snippet>) {
        deleteAll(entities)
    }

    override fun deleteAllByIdInBatch(ids: Iterable<Long>) {
        deleteAllById(ids)
    }

    override fun deleteAllInBatch() {
        deleteAll()
    }

    @Deprecated("Deprecated in Spring Data JPA")
    override fun getOne(id: Long): Snippet = findById(id).orElseThrow()

    @Deprecated("Deprecated in Spring Data JPA")
    override fun getById(id: Long): Snippet = findById(id).orElseThrow()

    override fun getReferenceById(id: Long): Snippet = findById(id).orElseThrow()

    override fun <S : Snippet> findAll(
        example: org.springframework.data.domain.Example<S>,
    ): List<S> = emptyList()

    override fun <S : Snippet> findAll(
        example: org.springframework.data.domain.Example<S>,
        sort: org.springframework.data.domain.Sort,
    ): List<S> = emptyList()

    override fun findAll(sort: org.springframework.data.domain.Sort): List<Snippet> = findAll()

    override fun findAll(
        pageable: org.springframework.data.domain.Pageable,
    ): org.springframework.data.domain.Page<Snippet> =
        org.springframework.data.domain
            .PageImpl(emptyList())

    override fun <S : Snippet> findOne(
        example: org.springframework.data.domain.Example<S>,
    ): Optional<S> = Optional.empty()

    override fun <S : Snippet> findAll(
        example: org.springframework.data.domain.Example<S>,
        pageable: org.springframework.data.domain.Pageable,
    ): org.springframework.data.domain.Page<S> =
        org.springframework.data.domain
            .PageImpl(emptyList())

    override fun <S : Snippet> count(example: org.springframework.data.domain.Example<S>): Long = 0

    override fun <S : Snippet> exists(
        example: org.springframework.data.domain.Example<S>,
    ): Boolean = false

    override fun <S : Snippet, R : Any> findBy(
        example: org.springframework.data.domain.Example<S>,
        queryFunction: java.util.function.Function<
            org.springframework.data.repository.query.FluentQuery
                .FetchableFluentQuery<S>,
            R,
        >,
    ): R = throw UnsupportedOperationException()
}

class FakeAssetServiceClient :
    AssetServiceClient(
        restTemplate =
            org.springframework.web.client
                .RestTemplate(),
        assetServiceUrl = "http://fake",
    ) {
    data class CreateOrUpdateCall(
        val container: String,
        val key: String,
        val content: String,
    )

    data class DeleteCall(
        val container: String,
        val key: String,
    )

    val createOrUpdateCalls = mutableListOf<CreateOrUpdateCall>()
    val deleteCalls = mutableListOf<DeleteCall>()

    override fun createOrUpdateAsset(
        container: String,
        key: String,
        content: String,
    ) {
        createOrUpdateCalls.add(CreateOrUpdateCall(container, key, content))
    }

    override fun deleteAsset(
        container: String,
        key: String,
    ) {
        deleteCalls.add(DeleteCall(container, key))
    }

    override fun getAsset(
        container: String,
        key: String,
    ): String = "fake content"
}
