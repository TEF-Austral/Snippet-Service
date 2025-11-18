package consumers.result

import handlers.TestingResultHandlerInt
import org.austral.ingsis.redis.RedisStreamConsumer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.stream.StreamReceiver

import org.springframework.stereotype.Component
import dtos.responses.TestingResultEvent
import java.time.Duration

@Component
@Profile("!test")
class TestingResultConsumer(
    @Value($$"${spring.redis.stream.testing.result.key}") streamKey: String,
    @Value($$"${spring.redis.consumer.group}") consumerGroup: String,
    private val handler: TestingResultHandlerInt,
    redis: RedisTemplate<String, String>,
) : RedisStreamConsumer<TestingResultEvent>(streamKey, consumerGroup, redis) {
    private val log = LoggerFactory.getLogger(TestingResultConsumer::class.java)

    override fun onMessage(record: ObjectRecord<String, TestingResultEvent>) {
        try {
            val event = record.value
            log.debug(
                "Received testing result message: testId=${event.testId}, snippetId=${event.snippetId}, requestId=${event.requestId}",
            )
            handler.handleTestingResult(event)
        } catch (e: Exception) {
            val stackTrace = e.stackTrace.firstOrNull()
            val location =
                stackTrace?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"
            log.error("Error processing testing result message at $location: ${e.message}", e)
        }
    }

    override fun options(): StreamReceiver.StreamReceiverOptions<
        String,
        ObjectRecord<
            String,
            TestingResultEvent,
        >,
    > =
        StreamReceiver.StreamReceiverOptions
            .builder()
            .pollTimeout(Duration.ofMillis(30000))
            .targetType(TestingResultEvent::class.java)
            .build()
}
