package snippet.consumers.notification

import notifications.AnalyzerRulesUpdatedEvent
import org.austral.ingsis.redis.RedisStreamConsumer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Component
import snippet.services.RulesUpdatedHandler
import java.time.Duration

@Component
@Profile("!test")
class AnalyzerRulesUpdatedConsumer(
    @Value("\${spring.redis.stream.rules.analyzer.updated}") streamKey: String,
    @Value("\${spring.redis.consumer.group}") consumerGroup: String,
    redis: RedisTemplate<String, String>,
    private val handler: RulesUpdatedHandler, // Inyecta el handler
) : RedisStreamConsumer<AnalyzerRulesUpdatedEvent>(streamKey, consumerGroup, redis) {

    override fun onMessage(record: ObjectRecord<String, AnalyzerRulesUpdatedEvent>) {
        val event = record.value
        println("📨 [Snippet Service] Recibido AnalyzerRulesUpdatedEvent para: ${event.userId}")
        handler.handleAnalyzerRulesUpdate(event.userId) // Llama al handler
    }

    override fun options(): StreamReceiver.StreamReceiverOptions<
        String,
        ObjectRecord<String, AnalyzerRulesUpdatedEvent>,
    > =
        StreamReceiver.StreamReceiverOptions
            .builder()
            .pollTimeout(Duration.ofMillis(10000))
            .targetType(AnalyzerRulesUpdatedEvent::class.java) // Mapea a nuestro DTO
            .build()
}
