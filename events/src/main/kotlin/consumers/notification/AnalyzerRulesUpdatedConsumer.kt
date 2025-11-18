package consumers.notification

import events.AnalyzerRulesUpdatedEvent
import handlers.rules.RuleType
import handlers.rules.RulesUpdatedHandler
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.austral.ingsis.redis.RedisStreamConsumer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Component
import java.time.Duration

@Component
@Profile("!test")
class AnalyzerRulesUpdatedConsumer(
    @Value("\${spring.redis.stream.rules.analyzer.updated}") streamKey: String,
    @Value("\${spring.redis.consumer.group}") consumerGroup: String,
    redis: RedisTemplate<String, String>,
    private val handler: RulesUpdatedHandler,
) : RedisStreamConsumer<AnalyzerRulesUpdatedEvent>(streamKey, consumerGroup, redis) {
    private val log = LoggerFactory.getLogger(AnalyzerRulesUpdatedConsumer::class.java)
    private val componentScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessage(record: ObjectRecord<String, AnalyzerRulesUpdatedEvent>) {
        try {
            val event = record.value
            log.info("Received analyzer rules updated event: userId=${event.userId}")

            componentScope.launch {
                try {
                    handler.handle(RuleType.Lint, event.userId)
                } catch (e: Exception) {
                    val stackTrace = e.stackTrace.firstOrNull()
                    val location =
                        stackTrace?.let { "${it.className}.${it.methodName}:${it.lineNumber}" }
                            ?: "Unknown"
                    log.error(
                        "Error handling analyzer rules update at $location: userId=${event.userId}, error=${e.message}",
                        e,
                    )
                }
            }
        } catch (e: Exception) {
            val stackTrace = e.stackTrace.firstOrNull()
            val location =
                stackTrace?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"
            log.error(
                "Error processing analyzer rules updated message at $location: ${e.message}",
                e,
            )
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

    @PreDestroy
    fun onComponentDestroy() {
        componentScope.cancel()
    }
}
