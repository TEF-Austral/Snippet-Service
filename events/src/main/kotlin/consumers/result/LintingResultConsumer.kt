package consumers.result

import handlers.LintingResultHandlerInt
import org.austral.ingsis.redis.RedisStreamConsumer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Component
import dtos.responses.LintingResultEvent
import java.time.Duration

@Component
@Profile("!test")
class LintingResultConsumer(
    @Value($$"${spring.redis.stream.linting.result.key}") streamKey: String,
    @Value($$"${spring.redis.consumer.group}") consumerGroup: String,
    redis: RedisTemplate<String, String>,
    private val handler: LintingResultHandlerInt,
) : RedisStreamConsumer<LintingResultEvent>(streamKey, consumerGroup, redis) {

    override fun onMessage(record: ObjectRecord<String, LintingResultEvent>) {
        try {
            val event = record.value
            handler.handleLintingResult(event)
        } catch (e: Exception) {
            println("[Snippet Service] Error processing message: ${e.message}")
        }
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
            .pollTimeout(Duration.ofMillis(30000))
            .targetType(LintingResultEvent::class.java)
            .build()
}
