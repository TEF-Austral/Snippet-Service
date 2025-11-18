package snippet.component

import component.AssetServiceClient
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

@ExtendWith(MockitoExtension::class)
class AssetServiceClientTest {

    @Mock
    private lateinit var restTemplate: RestTemplate

    private lateinit var assetServiceClient: AssetServiceClient

    private val assetServiceUrl = "http://asset-service"

    @BeforeEach
    fun setup() {
        assetServiceClient = AssetServiceClient(restTemplate, assetServiceUrl)
    }

    @Test
    fun `getAsset should return asset content successfully`() {
        val container = "snippets"
        val key = "test-key"
        val expectedContent = "println('Hello World')"
        val url = "$assetServiceUrl/$container/$key"

        `when`(restTemplate.getForObject(url, String::class.java)).thenReturn(expectedContent)

        val result = assetServiceClient.getAsset(container, key)

        assertEquals(expectedContent, result)
        verify(restTemplate).getForObject(url, String::class.java)
    }

    @Test
    fun `getAsset should throw exception when asset not found`() {
        val container = "snippets"
        val key = "non-existent-key"
        val url = "$assetServiceUrl/$container/$key"

        `when`(restTemplate.getForObject(url, String::class.java)).thenReturn(null)

        try {
            assetServiceClient.getAsset(container, key)
            throw AssertionError("Expected NoSuchElementException")
        } catch (e: NoSuchElementException) {
            assertEquals("Asset not found: $container/$key", e.message)
        }
    }

    @Test
    fun `createOrUpdateAsset should call REST API with correct parameters`() {
        val container = "snippets"
        val key = "test-key"
        val content = "println('Updated')"
        val url = "$assetServiceUrl/$container/$key"

        `when`(
            restTemplate.exchange(
                org.mockito.ArgumentMatchers.eq(url),
                org.mockito.ArgumentMatchers.eq(HttpMethod.PUT),
                org.mockito.ArgumentMatchers.any(HttpEntity::class.java),
                org.mockito.ArgumentMatchers.eq(String::class.java),
            ),
        ).thenReturn(ResponseEntity.ok("Success"))

        assetServiceClient.createOrUpdateAsset(container, key, content)

        verify(restTemplate).exchange(
            org.mockito.ArgumentMatchers.eq(url),
            org.mockito.ArgumentMatchers.eq(HttpMethod.PUT),
            org.mockito.ArgumentMatchers.any(HttpEntity::class.java),
            org.mockito.ArgumentMatchers.eq(String::class.java),
        )
    }

    @Test
    fun `createOrUpdateAsset should handle empty content`() {
        val container = "snippets"
        val key = "empty-key"
        val content = ""
        val url = "$assetServiceUrl/$container/$key"

        `when`(
            restTemplate.exchange(
                org.mockito.ArgumentMatchers.eq(url),
                org.mockito.ArgumentMatchers.eq(HttpMethod.PUT),
                org.mockito.ArgumentMatchers.any(HttpEntity::class.java),
                org.mockito.ArgumentMatchers.eq(String::class.java),
            ),
        ).thenReturn(ResponseEntity.ok("Success"))

        assetServiceClient.createOrUpdateAsset(container, key, content)

        verify(restTemplate).exchange(
            org.mockito.ArgumentMatchers.eq(url),
            org.mockito.ArgumentMatchers.eq(HttpMethod.PUT),
            org.mockito.ArgumentMatchers.any(HttpEntity::class.java),
            org.mockito.ArgumentMatchers.eq(String::class.java),
        )
    }

    @Test
    fun `deleteAsset should call REST API with correct parameters`() {
        val container = "snippets"
        val key = "test-key"
        val url = "$assetServiceUrl/$container/$key"

        assetServiceClient.deleteAsset(container, key)

        verify(restTemplate).delete(url)
    }

    @Test
    fun `deleteAsset should work with different containers`() {
        val container = "documents"
        val key = "doc-key"
        val url = "$assetServiceUrl/$container/$key"

        assetServiceClient.deleteAsset(container, key)

        verify(restTemplate).delete(url)
    }
}
