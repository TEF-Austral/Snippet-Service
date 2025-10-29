package events

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.connection.stream.StreamRecords
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class SnippetEventProducer(
    @Value("\${spring.redis.stream.snippet.key}") private val streamKey: String,
    private val redis: RedisTemplate<String, String>,
) {

    // Return the stream record id as String to avoid exposing Redis-specific types in the public API
    fun publishSnippetEvent(event: SnippetEvent): String? {
        println("📤 Publishing snippet event to stream: $streamKey")

        val record =
            StreamRecords
                .newRecord()
                .ofObject(event)
                .withStreamKey(streamKey)

        val id =
            // Using opsForStream with <String, String> and adding the record object which will be serialized
            redis
                .opsForStream<String, String>()
                .add(record)

        return id?.toString()
    }
}
