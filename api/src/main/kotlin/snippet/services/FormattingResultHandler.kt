package snippet.services

import org.springframework.stereotype.Service
import results.FormattingResultEvent
import snippet.component.AssetServiceClient
import snippet.repositories.SnippetRepository

@Service
class FormattingResultHandler(
    private val snippetRepository: SnippetRepository,
    private val assetServiceClient: AssetServiceClient,
) {
    private val log = org.slf4j.LoggerFactory.getLogger(FormattingResultHandler::class.java)

    fun handleFormattingResult(result: FormattingResultEvent) {
        log.info(
            "Processing formatting result for snippet ${result.snippetId}, success: ${result.success}",
        )
        try {
            println(
                "🔔 [Snippet Service] Processing formatting result for snippet ${result.snippetId}",
            )

            if (result.success && result.formattedContent != null) {
                val snippet =
                    snippetRepository
                        .findById(result.snippetId)
                        .orElseThrow {
                            NoSuchElementException(
                                "Snippet not found: ${result.snippetId}",
                            )
                        }

                assetServiceClient.createOrUpdateAsset(
                    container = snippet.bucketContainer,
                    key = snippet.bucketKey!!,
                    content = result.formattedContent!!,
                )

                log.warn("Snippet ${result.snippetId} formatted successfully")
                println("✅ [Snippet Service] Snippet ${result.snippetId} formatted successfully")
            } else {
                log.warn("Formatting failed for snippet ${result.snippetId}: ${result.error}")
                println(
                    "❌ [Snippet Service] Formatting failed for snippet ${result.snippetId}: ${result.error}",
                )
            }
        } catch (e: NoSuchElementException) {
            log.warn("Error processing formatting result: ${e.message}")
            println("❌ [Snippet Service] Error processing formatting result: ${e.message}")
        } catch (e: Exception) {
            log.warn(
                "Unexpected error processing formatting result for snippet ${result.snippetId}: ${e.message}",
            )
            println(
                "❌ [Snippet Service] Unexpected error processing formatting result for snippet ${result.snippetId}: ${e.message}",
            )
            e.printStackTrace()
        }
    }
}
