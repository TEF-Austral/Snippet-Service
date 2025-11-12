package consumers.notification

import events.AnalyzerRulesUpdatedEvent
import handlers.rules.RuleType
import handlers.rules.RulesUpdatedHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.austral.ingsis.redis.RedisStreamConsumer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Component
import java.time.Duration

// TODO SE PODRIA CAMBIAR GLOBALSCOPE POR CoroutineScope, porque el GlocbalScope
// creo que puede tarer problemas

@Component
@Profile("!test")
class AnalyzerRulesUpdatedConsumer(
    @Value("\${spring.redis.stream.rules.analyzer.updated}") streamKey: String,
    @Value("\${spring.redis.consumer.group}") consumerGroup: String,
    redis: RedisTemplate<String, String>,
    private val handler: RulesUpdatedHandler,
) : RedisStreamConsumer<AnalyzerRulesUpdatedEvent>(streamKey, consumerGroup, redis) {

    override fun onMessage(record: ObjectRecord<String, AnalyzerRulesUpdatedEvent>) {
        val event = record.value

        GlobalScope.launch(Dispatchers.IO) {
            handler.handle(RuleType.Lint, event.userId)
        }
    }

    override fun options(): StreamReceiver.StreamReceiverOptions<
        String,
        ObjectRecord<String, AnalyzerRulesUpdatedEvent>,
    > =
        StreamReceiver.StreamReceiverOptions
            .builder()
            .pollTimeout(Duration.ofMillis(10000))
            .targetType(AnalyzerRulesUpdatedEvent::class.java)
            .build()
}
