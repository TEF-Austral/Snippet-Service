package config

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.test.context.TestPropertySource
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest(classes = [RedisConfig::class])
@TestPropertySource(
    properties = [
        "spring.data.redis.host=my-test-host",
        "spring.data.redis.port=1234",
    ],
)
class RedisConfigTest {

    @Autowired
    private lateinit var lettuceConnectionFactory: LettuceConnectionFactory

    @Test
    fun `lettuceConnectionFactory bean should be configured correctly`() {
        assertNotNull(lettuceConnectionFactory)

        assertEquals("my-test-host", lettuceConnectionFactory.hostName)
        assertEquals(1234, lettuceConnectionFactory.port)
    }
}
