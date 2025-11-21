package handlers

import assets.AssetService
import handlers.utils.findSnippetOrThrow
import handlers.utils.handleException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import dtos.responses.FormattingResultEvent
import repositories.SnippetRepository

@Service
class FormattingResultHandler(
    private val snippetRepository: SnippetRepository,
    private val assetServiceClient: AssetService,
) : FormattingResultHandlerInt {
    private val log = LoggerFactory.getLogger(FormattingResultHandler::class.java)

    override fun handleFormattingResult(result: FormattingResultEvent) {
        try {
            process(result)
        } catch (e: Exception) {
            val stackTrace = e.stackTrace.firstOrNull()
            val location =
                stackTrace?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"
            log.error(
                "Error handling formatting result for snippet ${result.snippetId} at $location: ${e.message}",
                e,
            )
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
        log.info(
            "Processing formatting result for snippet: snippetId=${result.snippetId}, requestId=${result.requestId}",
        )
    }

    private fun handleSuccess(result: FormattingResultEvent) {
        val snippet = findSnippetOrThrow(snippetRepository, result.snippetId)

        if (snippet.bucketKey == null) {
            log.warn("Snippet ${result.snippetId} has no bucket key")
            throw IllegalStateException("Snippet has no bucket key")
        }

        val bucketKey = snippet.bucketKey!!

        assetServiceClient.createOrUpdateAsset(
            container = snippet.bucketContainer,
            key = bucketKey,
            content = result.formattedContent!!,
        )

        log.info(
            "Snippet formatted successfully: snippetId=${result.snippetId}, requestId=${result.requestId}, container=${snippet.bucketContainer}, key=$bucketKey",
        )
    }

    private fun handleFailure(result: FormattingResultEvent) {
        log.warn(
            "Formatting failed for snippet: snippetId=${result.snippetId}, requestId=${result.requestId}, error=${result.error}",
        )
    }
}
