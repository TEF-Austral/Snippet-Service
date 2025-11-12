package handlers.utils

import common.dtos.types.ComplianceStatus
import dtos.responses.LintingResultEvent
import entity.Snippet
import repositories.SnippetRepository

fun findSnippetOrThrow(
    snippetRepository: SnippetRepository,
    snippetId: Long,
): Snippet =
    snippetRepository.findById(snippetId).orElseThrow {
        NoSuchElementException("Snippet not found: $snippetId")
    }

fun handleException(
    e: Exception,
    snippetId: String,
) {
    if (e is NoSuchElementException) {
        println(
            "[Snippet Service] Error processing formatting result: ${e.message}",
        )
    }
    println(
        "[Snippet Service] Unexpected error processing formatting result for snippet $snippetId: ${e.message}",
    )
    e.printStackTrace()
}

private fun formatViolations(violations: List<dtos.responses.ViolationDTO>): String =
    violations.joinToString(separator = "\n") { v ->
        "Line ${v.line}, Column ${v.column}: ${v.message}"
    }

fun handleLintingSnippetResult(
    snippet: Snippet,
    result: LintingResultEvent,
): Snippet {
    val status: ComplianceStatus
    val error: String?

    if (result.isValid) {
        status = ComplianceStatus.COMPLIANT
        error = null
    } else {
        status = ComplianceStatus.NON_COMPLIANT
        error = formatViolations(result.violations)
    }

    return snippet.copy(
        complianceStatus = status,
        lastValidationError = error,
    )
}
