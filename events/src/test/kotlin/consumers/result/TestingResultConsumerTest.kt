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

// --- Clase Helper para testear métodos protegidos ---
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
// ----------------------------------------------------

class TestingResultConsumerTest {

    private lateinit var consumer: TestableTestingResultConsumer // Usamos la clase helper
    private val handler: TestingResultHandlerInt = mockk(relaxed = true)
    private val redisTemplate: RedisTemplate<String, String> = mockk(relaxed = true)

    @BeforeEach
    fun setup() {
        consumer =
            TestableTestingResultConsumer( // Instanciamos la clase helper
                streamKey = "test-stream",
                consumerGroup = "test-group",
                redis = redisTemplate,
                handler = handler,
            )
    }

    @Test
    fun `onMessage should call handler with event`() {
        // Arrange
        // CORREGIDO: Usamos 'outputs = listOf("OK")' en lugar de 'output = "OK"'
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

        // Act
        consumer.onMessage(record)

        // Assert
        verify(exactly = 1) { handler.handleTestingResult(event) }
    }

    @Test
    fun `onMessage should catch and log exception from handler`() {
        // Arrange
        // CORREGIDO: Usamos 'outputs = listOf("OK")'
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

        // Act & Assert
        assertDoesNotThrow {
            consumer.onMessage(record)
        }
        verify(exactly = 1) { handler.handleTestingResult(event) }
    }

    @Test
    fun `options should return correct configuration`() {
        // Act
        val options = consumer.options()

        // Assert
        assertEquals(Duration.ofMillis(30000), options.pollTimeout)

        assertTrue(TestingResultEvent::class.java == options.getTargetType())
    }
}
