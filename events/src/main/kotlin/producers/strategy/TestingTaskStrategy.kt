package producers.strategy

import AsyncTaskRequestContext
import org.springframework.stereotype.Component
import producers.TestingRequestProducer
import requests.TestingRequestEvent
import java.util.UUID

@Component
class TestingTaskStrategy(
    private val producer: TestingRequestProducer,
) : AsyncTaskStrategy {

    override fun canHandle(type: TaskType): Boolean = type == TaskType.TESTING

    override fun submit(context: AsyncTaskRequestContext): String {
        val requestId = UUID.randomUUID().toString()
        val event =
            TestingRequestEvent(
                requestId = requestId,
                snippetId = context.snippetId,
                bucketContainer = context.bucketContainer,
                bucketKey = context.bucketKey,
                version = context.version,
            )
        producer.emit(event)
        println(
            "[Snippet Service] Published testing REQUEST: $requestId for snippet: ${context.snippetId}",
        )
        return requestId
    }
}
