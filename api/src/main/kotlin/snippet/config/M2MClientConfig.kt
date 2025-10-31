package snippet.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor

import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.web.client.RestTemplate

@Configuration
class M2MClientConfig {

    @Bean
    fun m2mAuthorizedClientManager(
        clientRegistrationRepository: ClientRegistrationRepository,
        authorizedClientRepository: OAuth2AuthorizedClientRepository,
    ): OAuth2AuthorizedClientManager {
        val authorizedClientProvider =
            OAuth2AuthorizedClientProviderBuilder
                .builder()
                .clientCredentials()
                .build()

        val authorizedClientManager =
            DefaultOAuth2AuthorizedClientManager(
                clientRegistrationRepository,
                authorizedClientRepository,
            )
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
        return authorizedClientManager
    }

    /**
     * Crea el Bean de RestTemplate que tu AuthorizationServiceClient usará.
     * Le inyectamos un interceptor que añade el token M2M.
     */
    @Bean
    fun restTemplate(manager: OAuth2AuthorizedClientManager): RestTemplate {
        val restTemplate = RestTemplate()

        val interceptor =
            ClientHttpRequestInterceptor {
                request: HttpRequest,
                body: ByteArray,
                execution: ClientHttpRequestExecution,
                ->

                // 1. Construye la solicitud para el token M2M
                // "auth0-m2m" debe coincidir con el nombre en tus properties (SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_AUTH0_M2M_...)
                val authorizeRequest =
                    OAuth2AuthorizeRequest
                        .withClientRegistrationId("auth0-m2m")
                        .principal("SnippetServiceM2M") // Un nombre principal genérico
                        .build()

                // 2. Pide el token al manager (lo cacheará automáticamente)
                val authorizedClient =
                    manager.authorize(authorizeRequest)
                        ?: throw OAuth2AuthenticationException(
                            "No se pudo autorizar el cliente M2M",
                        )

                // 3. Añade el token a la petición saliente
                request.headers.add(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer ${authorizedClient.accessToken.tokenValue}",
                )

                // 4. Ejecuta la petición
                execution.execute(request, body)
            }

        // Asigna el interceptor al RestTemplate
        restTemplate.interceptors = listOf(interceptor)
        return restTemplate
    }
}
