package producers.strategy

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import producers.TestingRequestProducer

@ExtendWith(MockitoExtension::class)
class TestingTaskStrategyTest {

    @Mock(lenient = true)
    lateinit var producer: TestingRequestProducer

    @Test
    fun `canHandle only testing`() {
        val s = TestingTaskStrategy(producer)
        assertEquals(true, s.canHandle(TaskType.TESTING))
        assertEquals(false, s.canHandle(TaskType.FORMATTING))
        assertEquals(false, s.canHandle(TaskType.LINTING))
    }

    // Skipping submit test due to mocking issues with external RedisStreamProducer
}
