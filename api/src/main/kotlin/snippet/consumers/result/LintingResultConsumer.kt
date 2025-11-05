package snippet.consumers.result

import org.austral.ingsis.redis.RedisStreamConsumer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.stereotype.Component
import results.LintingResultEvent

@Component
@Profile("!test")
class LintingResultConsumer(
    @Value("\${redis.stream.linting.result.key}") streamKey: String,
    @Value("\${redis.consumer.group}") consumerGroup: String,
    private val handler: LintingResultHandler,
) : RedisStreamConsumer<LintingResultEvent>(streamKey, consumerGroup) {

    override fun onMessage(record: ObjectRecord<String, LintingResultEvent>) {
        val event = record.value
        println("📨 [Snippet Service] Received linting RESULT: ${event.requestId}")
        handler.handleLintingResult(event)
    }
}
