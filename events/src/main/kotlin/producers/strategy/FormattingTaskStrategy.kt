package producers.strategy

import AsyncTaskRequestContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import producers.FormattingRequestProducer
import requests.FormattingRequestEvent
import java.util.UUID

@Component
class FormattingTaskStrategy(
    private val producer: FormattingRequestProducer,
) : AsyncTaskStrategy {
    private val log = LoggerFactory.getLogger(FormattingTaskStrategy::class.java)

    override fun canHandle(type: TaskType): Boolean = type == TaskType.FORMATTING

    override fun submit(context: AsyncTaskRequestContext): String {
        val languageId = requireNotNull(context.languageId) { "languageId required for formatting" }
        val userId = requireNotNull(context.userId) { "userId required for formatting" }
        val requestId = UUID.randomUUID().toString()
        val event =
            FormattingRequestEvent(
                requestId = requestId,
                bucketContainer = context.bucketContainer,
                bucketKey = context.bucketKey,
                languageId = languageId,
                version = context.version,
                userId = userId,
                snippetId = context.snippetId,
            )
        producer.emit(event)
        log.info(
            "Published formatting request: requestId=$requestId, snippetId=${context.snippetId}, userId=$userId",
        )
        return requestId
    }
}
