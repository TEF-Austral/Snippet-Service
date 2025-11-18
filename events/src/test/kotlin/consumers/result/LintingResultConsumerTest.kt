package consumers.result

import dtos.responses.LintingResultEvent
import handlers.LintingResultHandlerInt
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

internal class TestableLintingResultConsumer(
    streamKey: String,
    consumerGroup: String,
    redis: RedisTemplate<String, String>,
    handler: LintingResultHandlerInt,
) : LintingResultConsumer(streamKey, consumerGroup, redis, handler) {

    public override fun onMessage(record: ObjectRecord<String, LintingResultEvent>) {
        super.onMessage(record)
    }

    public override fun options(): StreamReceiver.StreamReceiverOptions<
        String,
        ObjectRecord<String, LintingResultEvent>,
    > =
        super.options()
}

class LintingResultConsumerTest {

    private lateinit var consumer: TestableLintingResultConsumer
    private val handler: LintingResultHandlerInt = mockk(relaxed = true)
    private val redisTemplate: RedisTemplate<String, String> = mockk(relaxed = true)

    @BeforeEach
    fun setup() {
        consumer =
            TestableLintingResultConsumer(
                streamKey = "test-stream",
                consumerGroup = "test-group",
                redis = redisTemplate,
                handler = handler,
            )
    }

    @Test
    fun `onMessage should call handler with event`() {
        val event =
            LintingResultEvent(
                snippetId = 1L,
                requestId = "req-1",
                isValid = true,
                violations = emptyList(),
            )
        val record: ObjectRecord<String, LintingResultEvent> =
            mockk {
                every { value } returns event
            }

        consumer.onMessage(record)

        verify(exactly = 1) { handler.handleLintingResult(event) }
    }

    @Test
    fun `onMessage should catch and log exception from handler`() {
        val event =
            LintingResultEvent(
                snippetId = 1L,
                requestId = "req-1",
                isValid = true,
                violations = emptyList(),
            )
        val record: ObjectRecord<String, LintingResultEvent> =
            mockk {
                every { value } returns event
            }
        every { handler.handleLintingResult(event) } throws RuntimeException("Handler failed")

        assertDoesNotThrow {
            consumer.onMessage(record)
        }
        verify(exactly = 1) { handler.handleLintingResult(event) }
    }

    @Test
    fun `options should return correct configuration`() {
        val options = consumer.options()

        assertEquals(Duration.ofMillis(30000), options.pollTimeout)

        assertTrue(LintingResultEvent::class.java == options.getTargetType())
    }
}
