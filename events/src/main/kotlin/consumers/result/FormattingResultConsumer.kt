package consumers.result

import handlers.FormattingResultHandlerInt
import org.austral.ingsis.redis.RedisStreamConsumer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Component
import dtos.responses.FormattingResultEventDTO
import org.springframework.data.redis.core.RedisTemplate
import java.time.Duration

@Component
@Profile("!test")
class FormattingResultConsumer(
    @Value($$"${spring.redis.stream.formatting.result.key}") streamKey: String,
    @param:Value($$"${spring.redis.consumer.group}") private val consumerGroup: String,
    redisTemplate: RedisTemplate<String, String>,
    private val handler: FormattingResultHandlerInt,
) : RedisStreamConsumer<FormattingResultEventDTO>(streamKey, consumerGroup, redisTemplate) {
    private val log = LoggerFactory.getLogger(FormattingResultConsumer::class.java)

    override fun onMessage(record: ObjectRecord<String, FormattingResultEventDTO>) {
        try {
            val event = record.value
            log.debug(
                "Received formatting result message: snippetId=${event.snippetId}, requestId=${event.requestId}",
            )
            handler.handleFormattingResult(event)
        } catch (e: Exception) {
            val stackTrace = e.stackTrace.firstOrNull()
            val location =
                stackTrace?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"
            log.error("Error processing formatting result message at $location: ${e.message}", e)
        }
    }

    override fun options(): StreamReceiver.StreamReceiverOptions<
        String,
        ObjectRecord<String, FormattingResultEventDTO>,
    > =
        StreamReceiver.StreamReceiverOptions
            .builder()
            .pollTimeout(Duration.ofMillis(30000))
            .targetType(FormattingResultEventDTO::class.java)
            .build()
}
