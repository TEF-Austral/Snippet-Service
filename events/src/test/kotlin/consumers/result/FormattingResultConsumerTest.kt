package consumers.result

import dtos.responses.FormattingResultEvent
import handlers.FormattingResultHandlerInt
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

internal class TestableFormattingResultConsumer(
    streamKey: String,
    consumerGroup: String,
    redisTemplate: RedisTemplate<String, String>,
    handler: FormattingResultHandlerInt,
) : FormattingResultConsumer(streamKey, consumerGroup, redisTemplate, handler) {

    public override fun onMessage(record: ObjectRecord<String, FormattingResultEvent>) {
        super.onMessage(record)
    }

    public override fun options(): StreamReceiver.StreamReceiverOptions<
        String,
        ObjectRecord<String, FormattingResultEvent>,
    > =
        super.options()
}

class FormattingResultConsumerTest {

    private lateinit var consumer: TestableFormattingResultConsumer
    private val handler: FormattingResultHandlerInt = mockk(relaxed = true)
    private val redisTemplate: RedisTemplate<String, String> = mockk(relaxed = true)

    @BeforeEach
    fun setup() {
        consumer =
            TestableFormattingResultConsumer(
                streamKey = "test-stream",
                consumerGroup = "test-group",
                redisTemplate = redisTemplate,
                handler = handler,
            )
    }

    @Test
    fun `onMessage should call handler with event`() {
        val event =
            FormattingResultEvent(
                requestId = "req-1",
                snippetId = 1L,
                success = true,
                formattedContent = "content",
            )
        val record: ObjectRecord<String, FormattingResultEvent> =
            mockk {
                every { value } returns event
            }

        consumer.onMessage(record)

        verify(exactly = 1) { handler.handleFormattingResult(event) }
    }

    @Test
    fun `onMessage should catch and log exception from handler`() {
        val event =
            FormattingResultEvent(
                requestId = "req-1",
                snippetId = 1L,
                success = false,
                error = "fail",
            )
        val record: ObjectRecord<String, FormattingResultEvent> =
            mockk {
                every { value } returns event
            }
        every { handler.handleFormattingResult(event) } throws RuntimeException("Handler failed")

        assertDoesNotThrow {
            consumer.onMessage(record)
        }
        verify(exactly = 1) { handler.handleFormattingResult(event) }
    }

    @Test
    fun `options should return correct configuration`() {
        val options = consumer.options()

        assertEquals(Duration.ofMillis(30000), options.pollTimeout)

        assertTrue(FormattingResultEvent::class.java == options.getTargetType())
    }
}
