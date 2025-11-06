package snippet.consumers.result

import org.austral.ingsis.redis.RedisStreamConsumer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Component
import results.FormattingResultEvent
import org.springframework.data.redis.core.RedisTemplate
import snippet.services.FormattingResultHandler
import java.time.Duration

@Component
@Profile("!test")
class FormattingResultConsumer(
    @Value("\${spring.redis.stream.formatting.result.key}") streamKey: String,
    @Value("\${spring.redis.consumer.group}") consumerGroup: String,
    redis: RedisTemplate<String, String>,
    private val handler: FormattingResultHandler,
) : RedisStreamConsumer<FormattingResultEvent>(streamKey, consumerGroup, redis) {

    override fun onMessage(record: ObjectRecord<String, FormattingResultEvent>) {
        val event = record.value
        println("📨 [Snippet Service] Received formatting RESULT: ${event.requestId}")
        handler.handleFormattingResult(event)
    }

    override fun options(): StreamReceiver.StreamReceiverOptions<
        String,
        ObjectRecord<
            String,
            FormattingResultEvent,
        >,
    > =
        StreamReceiver.StreamReceiverOptions
            .builder()
            .pollTimeout(Duration.ofMillis(10000))
            .targetType(FormattingResultEvent::class.java)
            .build()
}
