package snippet.consumers.result

import org.austral.ingsis.redis.RedisStreamConsumer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.stream.StreamReceiver

import org.springframework.stereotype.Component
import results.TestingResultEvent
import snippet.services.TestingResultHandler
import java.time.Duration

@Component
@Profile("!test")
class TestingResultConsumer(
    @Value("\${spring.redis.stream.testing.result.key}") streamKey: String,
    @Value("\${spring.redis.consumer.group}") consumerGroup: String,
    private val handler: TestingResultHandler,
    redis: RedisTemplate<String, String>,
) : RedisStreamConsumer<TestingResultEvent>(streamKey, consumerGroup, redis) {

    override fun onMessage(record: ObjectRecord<String, TestingResultEvent>) {
        val event = record.value
        println("📨 [Snippet Service] Received testing RESULT for test: ${event.testId}")
        handler.handleTestingResult(event)
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
            .pollTimeout(Duration.ofMillis(10000))
            .targetType(TestingResultEvent::class.java)
            .build()
}
