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

    fun handleFormattingResult(result: FormattingResultEvent) {
        println("🔔 [Snippet Service] Processing formatting result for snippet ${result.snippetId}")

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

            println("✅ [Snippet Service] Snippet ${result.snippetId} formatted successfully")
        } else {
            println(
                "❌ [Snippet Service] Formatting failed for snippet ${result.snippetId}: ${result.error}",
            )
        }
    }
}
