package handlers.rules

import entity.Snippet
import org.springframework.data.domain.PageRequest
import repositories.SnippetRepository

class SnippetPageProcessor(
    private val snippetRepository: SnippetRepository,
) {

    fun processAllSnippets(
        userId: String,
        pageSize: Int,
        processSnippet: (Snippet) -> Unit,
    ): Long {
        var page = 0
        var totalSent = 0L

        while (true) {
            val pageable = PageRequest.of(page, pageSize)
            val snippetPage = snippetRepository.findByOwnerId(userId, pageable)

            snippetPage.content.forEach { snippet ->
                processSnippet(snippet)
            }
            totalSent += snippetPage.content.size

            if (!snippetPage.hasNext()) break
            page++
        }

        return totalSent
    }
}
