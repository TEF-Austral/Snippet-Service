package api.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

@Configuration
class M2MClientConfig {

    @Value($$"${auth0.audience}")
    private lateinit var audience: String

    @Bean
    fun m2mAuthorizedClientManager(
        clientRegistrationRepository: ClientRegistrationRepository,
        authorizedClientService: OAuth2AuthorizedClientService,
    ): OAuth2AuthorizedClientManager {
        val accessTokenResponseClient = createAccessTokenResponseClient()

        val authorizedClientProvider =
            OAuth2AuthorizedClientProviderBuilder
                .builder()
                .clientCredentials { configurer ->
                    configurer.accessTokenResponseClient(accessTokenResponseClient)
                }.build()
        val authorizedClientManager =
            AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository,
                authorizedClientService,
            )
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)

        return authorizedClientManager
    }

    private fun createAccessTokenResponseClient(): OAuth2AccessTokenResponseClient<
        OAuth2ClientCredentialsGrantRequest,
    > =
        OAuth2AccessTokenResponseClient { grantRequest ->
            val restTemplate = RestTemplate()

            val parameters = buildTokenRequestParameters(audience)
            val headers =
                buildTokenRequestHeaders(
                    grantRequest.clientRegistration.clientId,
                    grantRequest.clientRegistration.clientSecret,
                )

            val request = HttpEntity(parameters, headers)
            val response =
                restTemplate.postForEntity(
                    grantRequest.clientRegistration.providerDetails.tokenUri,
                    request,
                    Map::class.java,
                )

            val responseBody = response.body
            val accessToken = extractAccessTokenFromResponse(responseBody)
            val expiresIn = extractExpiresInFromResponse(responseBody)

            OAuth2AccessTokenResponse
                .withToken(accessToken)
                .tokenType(OAuth2AccessToken.TokenType.BEARER)
                .expiresIn(expiresIn)
                .build()
        }

    @Bean
    @Primary
    fun restTemplate(manager: OAuth2AuthorizedClientManager): RestTemplate {
        val restTemplate = RestTemplate()
        val oauth2Interceptor = createOAuth2Interceptor(manager)

        restTemplate.interceptors = listOf(oauth2Interceptor)

        return restTemplate
    }

    private fun createOAuth2Interceptor(
        manager: OAuth2AuthorizedClientManager,
    ): ClientHttpRequestInterceptor =
        ClientHttpRequestInterceptor { request, body, execution ->
            val authorizeRequest =
                OAuth2AuthorizeRequest
                    .withClientRegistrationId("auth0-m2m")
                    .principal("SnippetServiceM2M")
                    .build()

            val authorizedClient =
                manager.authorize(authorizeRequest)
                    ?: throw OAuth2AuthenticationException("M2M client authorization failed")

            request.headers.add(
                HttpHeaders.AUTHORIZATION,
                "Bearer ${authorizedClient.accessToken.tokenValue}",
            )

            execution.execute(request, body)
        }

    private fun buildTokenRequestParameters(audience: String): LinkedMultiValueMap<String, String> {
        val parameters = LinkedMultiValueMap<String, String>()
        parameters.add("grant_type", "client_credentials")
        parameters.add("audience", audience)
        return parameters
    }

    private fun buildTokenRequestHeaders(
        clientId: String,
        clientSecret: String,
    ): HttpHeaders {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.setBasicAuth(clientId, clientSecret)
        return headers
    }

    private fun extractAccessTokenFromResponse(responseBody: Map<*, *>?): String {
        val responseBody =
            responseBody ?: throw OAuth2AuthenticationException("Invalid token response")
        return responseBody["access_token"] as? String
            ?: throw OAuth2AuthenticationException("No access token")
    }

    private fun extractExpiresInFromResponse(responseBody: Map<*, *>?): Long {
        val responseBody =
            responseBody ?: throw OAuth2AuthenticationException("Invalid token response")
        return (responseBody["expires_in"] as? Number)?.toLong() ?: 3600L
    }
}
