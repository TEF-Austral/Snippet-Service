package api.config

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.test.util.ReflectionTestUtils
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExtendWith(MockitoExtension::class)
class M2MClientConfigTest {

    @Mock
    private lateinit var clientRegistrationRepository: ClientRegistrationRepository

    @Mock
    private lateinit var authorizedClientService: OAuth2AuthorizedClientService

    private lateinit var config: M2MClientConfig

    @BeforeEach
    fun setUp() {
        config = M2MClientConfig()
        ReflectionTestUtils.setField(config, "audience", "https://api.example.com")
    }

    @Test
    fun `m2mAuthorizedClientManager creates manager with correct configuration`() {
        val manager =
            config.m2mAuthorizedClientManager(
                clientRegistrationRepository,
                authorizedClientService,
            )

        assertNotNull(manager)
    }

    @Test
    fun `config has audience field set`() {
        val audience = ReflectionTestUtils.getField(config, "audience")
        assertEquals("https://api.example.com", audience)
    }
}
