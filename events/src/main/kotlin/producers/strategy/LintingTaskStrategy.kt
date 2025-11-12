package producers.strategy

import AsyncTaskRequestContext
import org.springframework.stereotype.Component
import producers.LintingRequestProducer
import requests.LintingRequestEvent
import java.util.UUID

@Component
class LintingTaskStrategy(
    private val producer: LintingRequestProducer,
) : AsyncTaskStrategy {
    override fun canHandle(type: TaskType): Boolean = type == TaskType.LINTING

    override fun submit(context: AsyncTaskRequestContext): String {
        val languageId = requireNotNull(context.languageId) { "languageId required for linting" }
        val userId = requireNotNull(context.userId) { "userId required for linting" }
        val requestId = UUID.randomUUID().toString()
        val event =
            LintingRequestEvent(
                requestId = requestId,
                bucketContainer = context.bucketContainer,
                bucketKey = context.bucketKey,
                languageId = languageId,
                version = context.version,
                userId = userId,
                snippetId = context.snippetId,
            )
        producer.emit(event)
        println(
            "[Snippet Service] Published linting REQUEST: $requestId for snippet: ${context.snippetId}",
        )
        return requestId
    }
}
