package utils

import common.dtos.types.ComplianceStatus
import dtos.responses.LintingResultEvent
import entity.Snippet

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
