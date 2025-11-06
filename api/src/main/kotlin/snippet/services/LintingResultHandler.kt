package snippet.services

import org.springframework.stereotype.Service
import results.LintingResultEvent
import snippet.entities.ComplianceStatus
import snippet.repositories.SnippetRepository

@Service
class LintingResultHandler(
    private val snippetRepository: SnippetRepository,
) {

    fun handleLintingResult(result: LintingResultEvent) {
        println("🔔 [Snippet Service] Processing linting result for snippet ${result.snippetId}")

        val snippet =
            snippetRepository
                .findById(result.snippetId)
                .orElseThrow { NoSuchElementException("Snippet not found: ${result.snippetId}") }

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
        println(
            "✅ [Snippet Service] Snippet ${result.snippetId} compliance updated: ${snippet.complianceStatus}",
        )
    }
}
