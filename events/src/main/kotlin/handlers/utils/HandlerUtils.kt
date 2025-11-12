package handlers.utils

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
