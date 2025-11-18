package consumers.result

import dtos.responses.TestingResultEvent
import handlers.TestingResultHandlerInt
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import java.time.Duration
import kotlin.test.assertEquals

internal class TestableTestingResultConsumer(
    streamKey: String,
    consumerGroup: String,
    handler: TestingResultHandlerInt,
    redis: RedisTemplate<String, String>,
) : TestingResultConsumer(streamKey, consumerGroup, handler, redis) {

    public override fun onMessage(record: ObjectRecord<String, TestingResultEvent>) {
        super.onMessage(record)
    }

    public override fun options(): StreamReceiver.StreamReceiverOptions<
        String,
        ObjectRecord<String, TestingResultEvent>,
    > =
        super.options()
}

class TestingResultConsumerTest {

    private lateinit var consumer: TestableTestingResultConsumer
    private val handler: TestingResultHandlerInt = mockk(relaxed = true)
    private val redisTemplate: RedisTemplate<String, String> = mockk(relaxed = true)

    @BeforeEach
    fun setup() {
        consumer =
            TestableTestingResultConsumer(
                streamKey = "test-stream",
                consumerGroup = "test-group",
                redis = redisTemplate,
                handler = handler,
            )
    }

    @Test
    fun `onMessage should call handler with event`() {
        val event =
            TestingResultEvent(
                testId = 1L,
                snippetId = 1L,
                requestId = "req-1",
                passed = true,
                outputs = listOf("OK"),
            )
        val record: ObjectRecord<String, TestingResultEvent> =
            mockk {
                every { value } returns event
            }

        consumer.onMessage(record)

        verify(exactly = 1) { handler.handleTestingResult(event) }
    }

    @Test
    fun `onMessage should catch and log exception from handler`() {
        val event =
            TestingResultEvent(
                testId = 1L,
                snippetId = 1L,
                requestId = "req-1",
                passed = true,
                outputs = listOf("OK"),
            )
        val record: ObjectRecord<String, TestingResultEvent> =
            mockk {
                every { value } returns event
            }
        every { handler.handleTestingResult(event) } throws RuntimeException("Handler failed")

        assertDoesNotThrow {
            consumer.onMessage(record)
        }
        verify(exactly = 1) { handler.handleTestingResult(event) }
    }

    @Test
    fun `options should return correct configuration`() {
        val options = consumer.options()

        assertEquals(Duration.ofMillis(30000), options.pollTimeout)

        assertTrue(TestingResultEvent::class.java == options.getTargetType())
    }
}
