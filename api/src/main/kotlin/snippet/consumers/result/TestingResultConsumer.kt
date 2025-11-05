package snippet.consumers.result


import org.austral.ingsis.redis.RedisStreamConsumer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import results.TestingResultEvent


@Component
@Profile("!test")
class TestingResultConsumer(
    @Value("\${redis.stream.testing.result.key}") streamKey: String,
    @Value("\${redis.consumer.group}") consumerGroup: String,
    private val handler: TestingResultHandler,
) : RedisStreamConsumer<TestingResultEvent>(streamKey, consumerGroup) {

    override fun onMessage(record: ObjectRecord<String, TestingResultEvent>) {
        val event = record.value
        println("📨 [Snippet Service] Received testing RESULT for test: ${event.testId}")
        handler.handleTestingResult(event)
    }
}