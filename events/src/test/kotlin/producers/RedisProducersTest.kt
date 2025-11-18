package producers

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
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

    @Autowired
    private lateinit var lintingRequestProducer: LintingRequestProducer

    @Autowired
    private lateinit var formattingRequestProducer: FormattingRequestProducer

    @Autowired
    private lateinit var testingRequestProducer: TestingRequestProducer

    @Test
    fun `producers should be created by Spring context`() {
        assertNotNull(lintingRequestProducer)
        assertNotNull(formattingRequestProducer)
        assertNotNull(testingRequestProducer)
    }
}
