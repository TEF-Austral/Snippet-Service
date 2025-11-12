package consumers.notification

import events.FormattingRulesUpdatedEvent
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

@Component
@Profile("!test")
class FormattingRulesUpdatedConsumer(
    @Value("\${spring.redis.stream.rules.formatter.updated}") streamKey: String,
    @Value("\${spring.redis.consumer.group}") consumerGroup: String,
    redis: RedisTemplate<String, String>,
    private val handler: RulesUpdatedHandler,
) : RedisStreamConsumer<FormattingRulesUpdatedEvent>(streamKey, consumerGroup, redis) {

    override fun onMessage(record: ObjectRecord<String, FormattingRulesUpdatedEvent>) {
        val event = record.value

        GlobalScope.launch(Dispatchers.IO) {
            handler.handle(RuleType.Format, event.userId)
        }
    }

    override fun options(): StreamReceiver.StreamReceiverOptions<
        String,
        ObjectRecord<String, FormattingRulesUpdatedEvent>,
    > =
        StreamReceiver.StreamReceiverOptions
            .builder()
            .pollTimeout(Duration.ofMillis(30000))
            .targetType(FormattingRulesUpdatedEvent::class.java)
            .build()
}
