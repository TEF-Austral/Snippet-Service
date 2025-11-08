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
    @Value("\${spring.redis.consumer.group}") private val consumerGroup: String,
    private val redisTemplate: RedisTemplate<String, String>,
    private val handler: FormattingResultHandler,
) : RedisStreamConsumer<FormattingResultEvent>(streamKey, consumerGroup, redisTemplate) {

    override fun onMessage(record: ObjectRecord<String, FormattingResultEvent>) {
        try {
            val event = record.value
            println("📨 [Snippet Service] Received formatting RESULT: ${event.requestId}")
            handler.handleFormattingResult(event)

            // ACK explícito después de procesamiento exitoso
            redisTemplate.opsForStream<String, Any>().acknowledge(
                record.stream ?: streamKey,
                consumerGroup,
                record.id,
            )
            println("✅ [Snippet Service] ACK sent for: ${event.requestId}")
        } catch (e: Exception) {
            println("❌ [Snippet Service] Error processing message: ${e.message}")
            // No hacer ACK en caso de error, para que se reintente
        }
    }

    override fun options(): StreamReceiver.StreamReceiverOptions<
        String,
        ObjectRecord<String, FormattingResultEvent>,
    > =
        StreamReceiver.StreamReceiverOptions
            .builder()
            .pollTimeout(Duration.ofMillis(30000)) // ← Aumentar timeout
            .targetType(FormattingResultEvent::class.java)
            .build()
}
