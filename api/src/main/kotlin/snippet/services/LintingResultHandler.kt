package snippet.services

import org.springframework.stereotype.Service
import results.LintingResultEvent
import snippet.entities.ComplianceStatus
import snippet.repositories.SnippetRepository

@Service
class LintingResultHandler(
    private val snippetRepository: SnippetRepository,
) {
    private val log = org.slf4j.LoggerFactory.getLogger(LintingResultHandler::class.java)

    fun handleLintingResult(result: LintingResultEvent) {
        log.info(
            "Processing linting result for snippet ${result.snippetId}, isValid: ${result.isValid}",
        )
        try {
            println(
                "🔔 [Snippet Service] Processing linting result for snippet ${result.snippetId}",
            )

            val snippet =
                snippetRepository
                    .findById(result.snippetId)
                    .orElseThrow {
                        NoSuchElementException(
                            "Snippet not found: ${result.snippetId}",
                        )
                    }

            if (result.isValid) {
                snippet.complianceStatus = ComplianceStatus.COMPLIANT
                snippet.lastValidationError = null
            } else {
                snippet.complianceStatus = ComplianceStatus.NON_COMPLIANT
                snippet.lastValidationError =
                    result.violations.joinToString("\n") {
                        "Line ${it.line}, Column ${it.column}: ${it.message}"
                    }
            }

            snippetRepository.save(snippet)
            log.warn("Snippet ${result.snippetId} compliance updated: ${snippet.complianceStatus}")
            println(
                "✅ [Snippet Service] Snippet ${result.snippetId} compliance updated: ${snippet.complianceStatus}",
            )
        } catch (e: NoSuchElementException) {
            log.warn("Error processing linting result: ${e.message}")
            println("❌ [Snippet Service] Error processing linting result: ${e.message}")
        } catch (e: Exception) {
            log.warn(
                "Unexpected error processing linting result for snippet ${result.snippetId}: ${e.message}",
            )
            println(
                "❌ [Snippet Service] Unexpected error processing linting result for snippet ${result.snippetId}: ${e.message}",
            )
            e.printStackTrace()
        }
    }
}
