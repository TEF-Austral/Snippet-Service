package handlers

import component.AssetService
import handlers.utils.findSnippetOrThrow
import handlers.utils.handleException
import org.springframework.stereotype.Service
import dtos.responses.FormattingResultEvent
import repositories.SnippetRepository

@Service
class FormattingResultHandler(
    private val snippetRepository: SnippetRepository,
    private val assetServiceClient: AssetService,
) : FormattingResultHandlerInt {

    override fun handleFormattingResult(result: FormattingResultEvent) {
        try {
            process(result)
        } catch (e: Exception) {
            handleException(e, result.snippetId.toString())
        }
    }

    private fun process(result: FormattingResultEvent) {
        logProcessing(result)
        if (isSuccessful(result)) handleSuccess(result) else handleFailure(result)
    }

    private fun isSuccessful(result: FormattingResultEvent) =
        result.success && result.formattedContent != null

    private fun logProcessing(result: FormattingResultEvent) {
        println("[Snippet Service] Processing formatting result for snippet ${result.snippetId}")
    }

    private fun handleSuccess(result: FormattingResultEvent) {
        val snippet = findSnippetOrThrow(snippetRepository, result.snippetId)

        assetServiceClient.createOrUpdateAsset(
            container = snippet.bucketContainer,
            key = snippet.bucketKey!!,
            content = result.formattedContent!!,
        )

        println("[Snippet Service] Snippet ${result.snippetId} formatted successfully")
    }

    private fun handleFailure(result: FormattingResultEvent) {
        println(
            "[Snippet Service] Formatting failed for snippet ${result.snippetId}: ${result.error}",
        )
    }
}
