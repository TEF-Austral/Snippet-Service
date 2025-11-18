package producers

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.TestPropertySource
import kotlin.test.assertNotNull

@SpringBootTest(
    classes = [
        LintingRequestProducer::class,
        FormattingRequestProducer::class,
        TestingRequestProducer::class,
    ],
)
@TestPropertySource(
    properties = [
        "spring.redis.stream.linting.request.key=test-lint-key",
        "spring.redis.stream.formatting.request.key=test-format-key",
        "spring.redis.stream.testing.request.key=test-test-key",
    ],
)
class RedisProducersTest {

    @MockBean
    private lateinit var redisTemplate: RedisTemplate<String, String>

    @Autowired
    private lateinit var lintingRequestProducer: LintingRequestProducer

    @Autowired
    private lateinit var formattingRequestProducer: FormattingRequestProducer

    @Autowired
    private lateinit var testingRequestProducer: TestingRequestProducer

    @Test
    fun `producers should be created by Spring context`() {
        // El simple hecho de que @Autowired funcione significa que
        // Spring pudo resolver las dependencias (@Value y RedisTemplate)
        // y llamar al constructor, cubriendo el código de esas clases.
        assertNotNull(lintingRequestProducer)
        assertNotNull(formattingRequestProducer)
        assertNotNull(testingRequestProducer)
    }
}
