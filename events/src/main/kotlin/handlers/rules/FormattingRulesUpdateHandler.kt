package handlers.rules

import AsyncTaskRequestContext
import entity.Snippet
import org.slf4j.LoggerFactory
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
    private val log = LoggerFactory.getLogger(FormattingRulesUpdateHandler::class.java)

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
        log.info("Received FORMAT rules update for user: userId=$userId")
    }

    private fun logFormattingRequest(snippetId: Long) {
        log.debug("Requesting reformat for snippet: snippetId=$snippetId")
    }

    private fun logSendError(
        snippet: Snippet,
        e: Exception,
    ) {
        val snippetId = snippet.id!!
        val stackTrace = e.stackTrace.firstOrNull()
        val location =
            stackTrace?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"
        log.error(
            "Error sending formatting request for snippet $snippetId at $location: ${e.message}",
            e,
        )
    }

    private fun logCompletionSuccess(totalSent: Long) {
        log.info("Formatting requests sent successfully: totalSnippets=$totalSent")
    }

    private fun logCriticalError(
        userId: String,
        e: Exception,
    ) {
        val stackTrace = e.stackTrace.firstOrNull()
        val location =
            stackTrace?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"
        log.error(
            "Critical error processing format rules update for user $userId at $location: ${e.message}",
            e,
        )
    }
}
