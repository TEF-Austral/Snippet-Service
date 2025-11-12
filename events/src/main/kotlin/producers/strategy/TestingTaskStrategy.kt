package producers.strategy

import AsyncTaskRequestContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import producers.TestingRequestProducer
import requests.TestingRequestEvent
import java.util.UUID

@Component
class TestingTaskStrategy(
    private val producer: TestingRequestProducer,
) : AsyncTaskStrategy {
    private val log = LoggerFactory.getLogger(TestingTaskStrategy::class.java)

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
        log.info("Published testing request: requestId=$requestId, snippetId=${context.snippetId}")
        return requestId
    }
}
