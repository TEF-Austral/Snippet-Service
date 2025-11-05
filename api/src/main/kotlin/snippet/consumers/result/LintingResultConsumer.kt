package snippet.consumers.result

import org.austral.ingsis.redis.RedisStreamConsumer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Component
import results.LintingResultEvent
import snippet.services.LintingResultHandler
import java.time.Duration

@Component
@Profile("!test")
class LintingResultConsumer(
    @Value("\${redis.stream.linting.result.key}") streamKey: String,
    @Value("\${redis.consumer.group}") consumerGroup: String,
    redis: RedisTemplate<String, String>,
    private val handler: LintingResultHandler,
) : RedisStreamConsumer<LintingResultEvent>(streamKey, consumerGroup, redis) {

    override fun onMessage(record: ObjectRecord<String, LintingResultEvent>) {
        val event = record.value
        println("📨 [Snippet Service] Received linting RESULT: ${event.requestId}")
        handler.handleLintingResult(event)
    }

    override fun options(): StreamReceiver.StreamReceiverOptions<
        String,
        ObjectRecord<
            String,
            LintingResultEvent,
        >,
    > =
        StreamReceiver.StreamReceiverOptions
            .builder()
            .pollTimeout(Duration.ofMillis(10000))
            .targetType(LintingResultEvent::class.java)
            .build()
}
