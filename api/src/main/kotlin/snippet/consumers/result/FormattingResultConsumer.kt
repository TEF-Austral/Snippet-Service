package snippet.consumers.result

import events.FormattingResultEvent
import org.austral.ingsis.redis.RedisStreamConsumer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.stereotype.Component
import results.FormattingResultEvent
import snippet.services.FormattingResultHandler

@Component
@Profile("!test")
class FormattingResultConsumer(
    @Value("\${redis.stream.formatting.result.key}") streamKey: String,
    @Value("\${redis.consumer.group}") consumerGroup: String,
    private val handler: FormattingResultHandler,
) : RedisStreamConsumer<FormattingResultEvent>(streamKey, consumerGroup) {

    override fun onMessage(record: ObjectRecord<String, FormattingResultEvent>) {
        val event = record.value
        println("📨 [Snippet Service] Received formatting RESULT: ${event.requestId}")
        handler.handleFormattingResult(event)
    }
}



