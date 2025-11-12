package handlers.rules

import AsyncTaskRequestContext
import entity.Snippet
import org.springframework.stereotype.Component
import producers.AsyncTaskProducerInt
import producers.strategy.TaskType
import repositories.SnippetRepository

@Component
class FormattingRulesUpdateHandler(
    snippetRepository: SnippetRepository,
    private val asyncTaskProducer: AsyncTaskProducerInt,
    private val pageProcessor: SnippetPageProcessor = SnippetPageProcessor(snippetRepository),
) : RuleUpdateHandleInt {

    override fun canHandle(type: RuleType): Boolean = type == RuleType.Format

    override fun handle(
        userId: String,
        pageSize: Int,
    ) {
        try {
            logRulesUpdateReceived(userId)

            val totalSent =
                pageProcessor.processAllSnippets(userId, pageSize) { snippet ->
                    sendFormattingRequest(snippet, userId)
                }

            logCompletionSuccess(totalSent)
        } catch (e: Exception) {
            logCriticalError(userId, e)
        }
    }

    private fun sendFormattingRequest(
        snippet: Snippet,
        userId: String,
    ) {
        try {
            logFormattingRequest(snippet.id!!)

            val context =
                AsyncTaskRequestContext(
                    snippetId = snippet.id!!,
                    bucketContainer = snippet.bucketContainer,
                    bucketKey = snippet.bucketKey!!,
                    version = snippet.version,
                    languageId = snippet.language.name,
                    userId = userId,
                )

            asyncTaskProducer.request(
                TaskType.FORMATTING,
                context,
            )
        } catch (e: Exception) {
            logSendError(snippet, e)
        }
    }

    private fun logRulesUpdateReceived(userId: String) {
        println("[Snippet Service] Received FORMAT rules update for: $userId")
    }

    private fun logFormattingRequest(snippetId: Long) {
        println("... Requesting reformat for snippet $snippetId")
    }

    private fun logSendError(
        snippet: Snippet,
        e: Exception,
    ) {
        val snippetId = snippet.id!!
        println("Error sending formatting request for snippet $snippetId: ${e.message}")
        e.printStackTrace()
    }

    private fun logCompletionSuccess(totalSent: Long) {
        println("[Snippet Service] $totalSent snippets sent for reformatting.")
    }

    private fun logCriticalError(
        userId: String,
        e: Exception,
    ) {
        println(
            "[Snippet Service] Critical error processing format rules update for user $userId: ${e.message}",
        )
    }
}
