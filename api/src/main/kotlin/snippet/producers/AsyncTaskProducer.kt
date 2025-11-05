package snippet.producers

import org.austral.ingsis.redis.RedisStreamProducer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import request.FormattingRequestEvent
import request.LintingRequestEvent
import request.TestingRequestEvent
import java.util.UUID

@Component
class AsyncTaskProducer(
    @Autowired private val producer: RedisStreamProducer,
) {

    @Value("\${redis.stream.formatting.request.key}")
    private lateinit var formattingRequestKey: String

    @Value("\${redis.stream.linting.request.key}")
    private lateinit var lintingRequestKey: String

    @Value("\${redis.stream.testing.request.key}")
    private lateinit var testingRequestKey: String

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

        producer.emit(formattingRequestKey, event)
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

        producer.emit(lintingRequestKey, event)
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

        producer.emit(testingRequestKey, event)
        println(
            "📤 [Snippet Service] Published testing REQUEST: $requestId for snippet: $snippetId",
        )
        return requestId
    }
}
