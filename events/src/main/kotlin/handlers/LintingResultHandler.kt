package handlers

import org.springframework.stereotype.Service
import repositories.SnippetRepository
import dtos.responses.LintingResultEvent
import handlers.utils.findSnippetOrThrow
import handlers.utils.handleException
import utils.handleLintingSnippetResult

@Service
class LintingResultHandler(
    private val snippetRepository: SnippetRepository,
) : LintingResultHandlerInt {

    override fun handleLintingResult(result: LintingResultEvent) {
        try {
            logProcessing("for snippet ${result.snippetId}")

            val snippet = findSnippetOrThrow(snippetRepository, result.snippetId)

            val newSnippet = handleLintingSnippetResult(snippet, result)

            snippetRepository.save(newSnippet)

            println(
                "[Snippet Service] Snippet ${result.snippetId} compliance updated: ${newSnippet.complianceStatus}",
            )
        } catch (e: Exception) {
            handleException(e, result.snippetId.toString())
        }
    }

    private fun logProcessing(string: String) {
        println("[Snippet Service] Processing linting result: $string")
    }
}
