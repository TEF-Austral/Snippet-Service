package consumers.result

import handlers.FormattingResultHandlerInt
import org.austral.ingsis.redis.RedisStreamConsumer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Component
import dtos.responses.FormattingResultEvent
import org.springframework.data.redis.core.RedisTemplate
import java.time.Duration

@Component
@Profile("!test")
class FormattingResultConsumer(
    @Value($$"${spring.redis.stream.formatting.result.key}") streamKey: String,
    @param:Value($$"${spring.redis.consumer.group}") private val consumerGroup: String,
    redisTemplate: RedisTemplate<String, String>,
    private val handler: FormattingResultHandlerInt,
) : RedisStreamConsumer<FormattingResultEvent>(streamKey, consumerGroup, redisTemplate) {

    override fun onMessage(record: ObjectRecord<String, FormattingResultEvent>) {
        try {
            val event = record.value
            handler.handleFormattingResult(event)
        } catch (e: Exception) {
            println("[Snippet Service] Error processing message: ${e.message}")
        }
    }

    override fun options(): StreamReceiver.StreamReceiverOptions<
        String,
        ObjectRecord<String, FormattingResultEvent>,
    > =
        StreamReceiver.StreamReceiverOptions
            .builder()
            .pollTimeout(Duration.ofMillis(30000))
            .targetType(FormattingResultEvent::class.java)
            .build()
}
