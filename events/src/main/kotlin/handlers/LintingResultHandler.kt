package handlers

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import dtos.responses.LintingResultEvent
import handlers.utils.findSnippetOrThrow
import handlers.utils.handleException
import handlers.utils.handleLintingSnippetResult
import repositories.SnippetRepository

@Service
class LintingResultHandler(
    private val snippetRepository: SnippetRepository,
) : LintingResultHandlerInt {
    private val log = LoggerFactory.getLogger(LintingResultHandler::class.java)

    override fun handleLintingResult(result: LintingResultEvent) {
        try {
            log.info(
                "Processing linting result for snippet: snippetId=${result.snippetId}, requestId=${result.requestId}",
            )

            val snippet = findSnippetOrThrow(snippetRepository, result.snippetId)

            val newSnippet = handleLintingSnippetResult(snippet, result)

            snippetRepository.save(newSnippet)

            log.info(
                "Snippet compliance updated: snippetId=${result.snippetId}, complianceStatus=${newSnippet.complianceStatus}, requestId=${result.requestId}",
            )
        } catch (e: Exception) {
            val stackTrace = e.stackTrace.firstOrNull()
            val location =
                stackTrace?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"
            log.error(
                "Error handling linting result for snippet ${result.snippetId} at $location: ${e.message}",
                e,
            )
            handleException(e, result.snippetId.toString())
        }
    }
}
