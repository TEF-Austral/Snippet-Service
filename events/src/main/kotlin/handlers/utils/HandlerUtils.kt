package handlers.utils

import common.dtos.types.ComplianceStatus
import dtos.responses.LintingResultEvent
import entity.Snippet
import org.slf4j.LoggerFactory
import repositories.SnippetRepository

private val log = LoggerFactory.getLogger("handlers.utils.HandlerUtils")

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
    val stackTrace = e.stackTrace.firstOrNull()
    val location =
        stackTrace?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"

    if (e is NoSuchElementException) {
        log.warn("Error processing formatting result at $location: ${e.message}", e)
    } else {
        log.error(
            "Unexpected error processing formatting result for snippet $snippetId at $location: ${e.message}",
            e,
        )
    }
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
