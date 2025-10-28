package component

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class AssetServiceClient(
    private val restTemplate: RestTemplate,
    @param:Value("\${asset.service.url}") private val assetServiceUrl: String,
) {

    @Suppress("unused")
    fun getAsset(
        container: String,
        key: String,
    ): String {
        val url = "$assetServiceUrl/$container/$key"
        return restTemplate.getForObject(url, String::class.java)
            ?: throw NoSuchElementException("Asset not found: $container/$key")
    }

    fun createOrUpdateAsset(
        container: String,
        key: String,
        content: String,
    ) {
        val url = "$assetServiceUrl/$container/$key"
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.TEXT_PLAIN
            }
        val request = HttpEntity(content, headers)

        restTemplate.exchange(url, HttpMethod.PUT, request, String::class.java)
    }

    fun deleteAsset(
        container: String,
        key: String,
    ) {
        val url = "$assetServiceUrl/$container/$key"
        restTemplate.delete(url)
    }
}
