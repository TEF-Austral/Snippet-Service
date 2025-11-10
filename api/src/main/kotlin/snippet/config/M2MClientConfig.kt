package snippet.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpRequestExecution
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

    @Value("\${auth0.audience}")
    private lateinit var audience: String

    @Bean
    fun m2mAuthorizedClientManager(
        clientRegistrationRepository: ClientRegistrationRepository,
        authorizedClientService: OAuth2AuthorizedClientService, // Cambio aquí
    ): OAuth2AuthorizedClientManager {
        val accessTokenResponseClient =
            OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> { grantRequest ->
                val restTemplate = RestTemplate()

                val parameters = LinkedMultiValueMap<String, String>()
                parameters.add("grant_type", "client_credentials")
                parameters.add("audience", audience)

                val headers = HttpHeaders()
                headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
                headers.setBasicAuth(
                    grantRequest.clientRegistration.clientId,
                    grantRequest.clientRegistration.clientSecret,
                )

                val request = org.springframework.http.HttpEntity(parameters, headers)
                val response =
                    restTemplate.postForEntity(
                        grantRequest.clientRegistration.providerDetails.tokenUri,
                        request,
                        Map::class.java,
                    )

                val responseBody =
                    response.body ?: throw OAuth2AuthenticationException("Invalid token response")
                val accessToken =
                    responseBody["access_token"] as? String
                        ?: throw OAuth2AuthenticationException("No access token")
                val expiresIn = (responseBody["expires_in"] as? Number)?.toLong() ?: 3600L

                OAuth2AccessTokenResponse
                    .withToken(accessToken)
                    .tokenType(OAuth2AccessToken.TokenType.BEARER)
                    .expiresIn(expiresIn)
                    .build()
            }

        val authorizedClientProvider =
            OAuth2AuthorizedClientProviderBuilder
                .builder()
                .clientCredentials { configurer ->
                    configurer.accessTokenResponseClient(accessTokenResponseClient)
                }.build()

        // Usar AuthorizedClientServiceOAuth2AuthorizedClientManager en lugar de DefaultOAuth2AuthorizedClientManager
        val authorizedClientManager =
            AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository,
                authorizedClientService,
            )
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)

        return authorizedClientManager
    }

    @Bean
    fun restTemplate(manager: OAuth2AuthorizedClientManager): RestTemplate {
        val restTemplate = RestTemplate()

        val interceptor =
            ClientHttpRequestInterceptor {
                request: HttpRequest,
                body: ByteArray,
                execution: ClientHttpRequestExecution,
                ->
                val authorizeRequest =
                    OAuth2AuthorizeRequest
                        .withClientRegistrationId("auth0-m2m")
                        .principal("SnippetServiceM2M")
                        .build()

                val authorizedClient =
                    manager.authorize(authorizeRequest)
                        ?: throw OAuth2AuthenticationException(
                            "No se pudo autorizar el cliente M2M",
                        )

                request.headers.add(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer ${authorizedClient.accessToken.tokenValue}",
                )

                execution.execute(request, body)
            }

        restTemplate.interceptors = listOf(interceptor)
        return restTemplate
    }
}
