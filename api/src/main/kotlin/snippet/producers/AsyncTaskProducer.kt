package snippet.producers

import org.austral.ingsis.redis.RedisStreamProducer
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import request.FormattingRequestEvent
import request.LintingRequestEvent
import request.TestingRequestEvent
import java.util.UUID

@Component
class FormattingRequestProducer(
    @Value("\${spring.redis.stream.formatting.request.key}") streamKey: String,
    redis: RedisTemplate<String, String>,
) : RedisStreamProducer(streamKey, redis)

@Component
class LintingRequestProducer(
    @Value("\${spring.redis.stream.linting.request.key}") streamKey: String,
    redis: RedisTemplate<String, String>,
) : RedisStreamProducer(streamKey, redis)

@Component
class TestingRequestProducer(
    @Value("\${spring.redis.stream.testing.request.key}") streamKey: String,
    redis: RedisTemplate<String, String>,
) : RedisStreamProducer(streamKey, redis)

@Component
class AsyncTaskProducer(
    private val formattingProducer: FormattingRequestProducer,
    private val lintingProducer: LintingRequestProducer,
    private val testingProducer: TestingRequestProducer,
) {

    fun requestFormatting(
        snippetId: Long,
        bucketContainer: String,
        bucketKey: String,
        version: String,
        userId: String,
    ): String {
        val requestId = UUID.randomUUID().toString()
        val event =
            FormattingRequestEvent(
                requestId = requestId,
                snippetId = snippetId,
                bucketContainer = bucketContainer,
                bucketKey = bucketKey,
                version = version,
                userId = userId,
            )

        formattingProducer.emit(event)
        println(
            "📤 [Snippet Service] Published formatting REQUEST: $requestId for snippet: $snippetId",
        )
        return requestId
    }

    fun requestLinting(
        snippetId: Long,
        bucketContainer: String,
        bucketKey: String,
        version: String,
        userId: String,
    ): String {
        val requestId = UUID.randomUUID().toString()
        val event =
            LintingRequestEvent(
                requestId = requestId,
                snippetId = snippetId,
                bucketContainer = bucketContainer,
                bucketKey = bucketKey,
                version = version,
                userId = userId,
            )

        lintingProducer.emit(event)
        println(
            "📤 [Snippet Service] Published linting REQUEST: $requestId for snippet: $snippetId",
        )
        return requestId
    }

    fun requestTesting(
        snippetId: Long,
        bucketContainer: String,
        bucketKey: String,
        version: String,
        testId: Long,
    ): String {
        val requestId = UUID.randomUUID().toString()
        val event =
            TestingRequestEvent(
                requestId = requestId,
                snippetId = snippetId,
                bucketContainer = bucketContainer,
                bucketKey = bucketKey,
                version = version,
                testId = testId,
            )

        testingProducer.emit(event)
        println(
            "📤 [Snippet Service] Published testing REQUEST: $requestId for snippet: $snippetId",
        )
        return requestId
    }
}
