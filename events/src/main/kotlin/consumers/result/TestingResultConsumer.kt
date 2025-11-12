package consumers.result

import handlers.TestingResultHandlerInt
import org.austral.ingsis.redis.RedisStreamConsumer
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

    override fun onMessage(record: ObjectRecord<String, TestingResultEvent>) {
        try {
            val event = record.value
            handler.handleTestingResult(event)
        } catch (e: Exception) {
            println("[Snippet Service] Error processing message: ${e.message}")
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
