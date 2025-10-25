package clients

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AssetServiceClientTest {

    @Test
    fun `getAsset returns content when asset exists`() {
        val fakeRestTemplate = FakeRestTemplate()
        val assetServiceUrl = "http://asset-service"
        val client = AssetServiceClient(fakeRestTemplate, assetServiceUrl)

        fakeRestTemplate.getForObjectResult = "Test content"

        val result = client.getAsset("snippets", "test-key")

        assertEquals("Test content", result)
        assertEquals("$assetServiceUrl/snippets/test-key", fakeRestTemplate.lastGetUrl)
    }

    @Test
    fun `getAsset throws NoSuchElementException when asset not found`() {
        val fakeRestTemplate = FakeRestTemplate()
        val assetServiceUrl = "http://asset-service"
        val client = AssetServiceClient(fakeRestTemplate, assetServiceUrl)

        fakeRestTemplate.getForObjectResult = null

        val exception =
            assertThrows<NoSuchElementException> {
                client.getAsset("snippets", "non-existent")
            }

        assertEquals("Asset not found: snippets/non-existent", exception.message)
    }

    @Test
    fun `getAsset constructs correct URL with container and key`() {
        val fakeRestTemplate = FakeRestTemplate()
        val assetServiceUrl = "http://asset-service"
        val client = AssetServiceClient(fakeRestTemplate, assetServiceUrl)

        fakeRestTemplate.getForObjectResult = "content"

        client.getAsset("my-container", "my-key")

        assertEquals("$assetServiceUrl/my-container/my-key", fakeRestTemplate.lastGetUrl)
    }

    @Test
    fun `createOrUpdateAsset sends PUT request with correct content`() {
        val fakeRestTemplate = FakeRestTemplate()
        val assetServiceUrl = "http://asset-service"
        val client = AssetServiceClient(fakeRestTemplate, assetServiceUrl)

        val content = "New content"
        client.createOrUpdateAsset("snippets", "test-key", content)

        assertEquals("$assetServiceUrl/snippets/test-key", fakeRestTemplate.lastExchangeUrl)
        assertEquals(HttpMethod.PUT, fakeRestTemplate.lastExchangeMethod)
        assertEquals(content, fakeRestTemplate.lastExchangeEntity?.body)
    }

    @Test
    fun `createOrUpdateAsset sends request with TEXT_PLAIN content type`() {
        val fakeRestTemplate = FakeRestTemplate()
        val assetServiceUrl = "http://asset-service"
        val client = AssetServiceClient(fakeRestTemplate, assetServiceUrl)

        val content = "Content with special characters: !@#$%"
        client.createOrUpdateAsset("snippets", "key", content)

        assertNotNull(fakeRestTemplate.lastExchangeEntity)
        val entity = fakeRestTemplate.lastExchangeEntity!!
        assertNotNull(entity.headers.contentType)
        assertEquals(MediaType.TEXT_PLAIN, entity.headers.contentType)
        assertEquals(content, entity.body)
    }

    @Test
    fun `createOrUpdateAsset constructs correct URL`() {
        val fakeRestTemplate = FakeRestTemplate()
        val assetServiceUrl = "http://asset-service"
        val client = AssetServiceClient(fakeRestTemplate, assetServiceUrl)

        client.createOrUpdateAsset("documents", "doc-123", "Document content")

        assertEquals("$assetServiceUrl/documents/doc-123", fakeRestTemplate.lastExchangeUrl)
    }

    @Test
    fun `deleteAsset sends DELETE request to correct URL`() {
        val fakeRestTemplate = FakeRestTemplate()
        val assetServiceUrl = "http://asset-service"
        val client = AssetServiceClient(fakeRestTemplate, assetServiceUrl)

        client.deleteAsset("snippets", "test-key")

        assertEquals("$assetServiceUrl/snippets/test-key", fakeRestTemplate.lastDeleteUrl)
    }

    @Test
    fun `deleteAsset constructs correct URL with container and key`() {
        val fakeRestTemplate = FakeRestTemplate()
        val assetServiceUrl = "http://asset-service"
        val client = AssetServiceClient(fakeRestTemplate, assetServiceUrl)

        client.deleteAsset("archives", "archive-456")

        assertEquals("$assetServiceUrl/archives/archive-456", fakeRestTemplate.lastDeleteUrl)
    }

    @Test
    fun `getAsset propagates HTTP errors from RestTemplate`() {
        val fakeRestTemplate = FakeRestTemplate()
        val assetServiceUrl = "http://asset-service"
        val client = AssetServiceClient(fakeRestTemplate, assetServiceUrl)

        fakeRestTemplate.throwOnGet = HttpClientErrorException(HttpStatus.NOT_FOUND)

        assertThrows<HttpClientErrorException> {
            client.getAsset("snippets", "missing")
        }
    }

    @Test
    fun `createOrUpdateAsset propagates HTTP errors from RestTemplate`() {
        val fakeRestTemplate = FakeRestTemplate()
        val assetServiceUrl = "http://asset-service"
        val client = AssetServiceClient(fakeRestTemplate, assetServiceUrl)

        fakeRestTemplate.throwOnExchange =
            HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR)

        assertThrows<HttpClientErrorException> {
            client.createOrUpdateAsset("snippets", "key", "content")
        }
    }

    @Test
    fun `deleteAsset propagates HTTP errors from RestTemplate`() {
        val fakeRestTemplate = FakeRestTemplate()
        val assetServiceUrl = "http://asset-service"
        val client = AssetServiceClient(fakeRestTemplate, assetServiceUrl)

        fakeRestTemplate.throwOnDelete = HttpClientErrorException(HttpStatus.FORBIDDEN)

        assertThrows<HttpClientErrorException> {
            client.deleteAsset("snippets", "protected-key")
        }
    }

    @Test
    fun `createOrUpdateAsset handles empty content`() {
        val fakeRestTemplate = FakeRestTemplate()
        val assetServiceUrl = "http://asset-service"
        val client = AssetServiceClient(fakeRestTemplate, assetServiceUrl)

        client.createOrUpdateAsset("snippets", "empty-key", "")

        assertEquals("$assetServiceUrl/snippets/empty-key", fakeRestTemplate.lastExchangeUrl)
        assertEquals("", fakeRestTemplate.lastExchangeEntity?.body)
    }

    @Test
    fun `createOrUpdateAsset handles multiline content`() {
        val fakeRestTemplate = FakeRestTemplate()
        val assetServiceUrl = "http://asset-service"
        val client = AssetServiceClient(fakeRestTemplate, assetServiceUrl)

        val multilineContent =
            """
            line 1
            line 2
            line 3
            """.trimIndent()

        client.createOrUpdateAsset("snippets", "multiline", multilineContent)

        assertEquals(multilineContent, fakeRestTemplate.lastExchangeEntity?.body)
    }

    @Test
    fun `getAsset with different containers and keys`() {
        val fakeRestTemplate = FakeRestTemplate()
        val assetServiceUrl = "http://asset-service"
        val client = AssetServiceClient(fakeRestTemplate, assetServiceUrl)

        fakeRestTemplate.getForObjectResult = "content1"
        client.getAsset("container1", "key1")
        assertEquals("$assetServiceUrl/container1/key1", fakeRestTemplate.lastGetUrl)

        fakeRestTemplate.getForObjectResult = "content2"
        client.getAsset("container2", "key2")
        assertEquals("$assetServiceUrl/container2/key2", fakeRestTemplate.lastGetUrl)
    }

    @Test
    fun `multiple operations track calls correctly`() {
        val fakeRestTemplate = FakeRestTemplate()
        val assetServiceUrl = "http://asset-service"
        val client = AssetServiceClient(fakeRestTemplate, assetServiceUrl)

        fakeRestTemplate.getForObjectResult = "content"
        client.getAsset("snippets", "key1")
        client.createOrUpdateAsset("snippets", "key2", "new content")
        client.deleteAsset("snippets", "key3")

        assertTrue(fakeRestTemplate.lastGetUrl!!.contains("key1"))
        assertTrue(fakeRestTemplate.lastExchangeUrl!!.contains("key2"))
        assertTrue(fakeRestTemplate.lastDeleteUrl!!.contains("key3"))
    }
}

class FakeRestTemplate : RestTemplate() {
    var getForObjectResult: String? = "default content"
    var lastGetUrl: String? = null
    var throwOnGet: Exception? = null

    var lastExchangeUrl: String? = null
    var lastExchangeMethod: HttpMethod? = null
    var lastExchangeEntity: HttpEntity<*>? = null
    var throwOnExchange: Exception? = null

    var lastDeleteUrl: String? = null
    var throwOnDelete: Exception? = null

    override fun <T : Any?> getForObject(
        url: String,
        responseType: Class<T>,
        vararg uriVariables: Any,
    ): T? {
        lastGetUrl = url
        throwOnGet?.let { throw it }
        @Suppress("UNCHECKED_CAST")
        return getForObjectResult as T?
    }

    override fun <T : Any?> exchange(
        url: String,
        method: HttpMethod,
        requestEntity: HttpEntity<*>?,
        responseType: Class<T>,
        vararg uriVariables: Any,
    ): ResponseEntity<T> {
        lastExchangeUrl = url
        lastExchangeMethod = method
        lastExchangeEntity = requestEntity
        throwOnExchange?.let { throw it }
        @Suppress("UNCHECKED_CAST")
        return ResponseEntity.ok(null as T)
    }

    override fun delete(
        url: String,
        vararg uriVariables: Any,
    ) {
        lastDeleteUrl = url
        throwOnDelete?.let { throw it }
    }
}
