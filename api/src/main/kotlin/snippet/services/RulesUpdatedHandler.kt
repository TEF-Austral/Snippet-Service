package snippet.services

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import snippet.producers.AsyncTaskProducer
import snippet.repositories.SnippetRepository

@Service
class RulesUpdatedHandler(
    private val snippetRepository: SnippetRepository,
    private val asyncTaskProducer: AsyncTaskProducer,
) {

    companion object {
        private const val PAGE_SIZE = 100 // Procesar en lotes de 100
    }

    /**
     * Dispara el re-linting (validación) para todos los snippets de un usuario.
     */
    fun handleAnalyzerRulesUpdate(userId: String) {
        println("🔔 [Snippet Service] Recibida actualización de reglas de LINTING para: $userId")
        println("... Buscando snippets para re-validar...")

        var page = 0
        var totalSent = 0L
        while (true) {
            val pageable = PageRequest.of(page, PAGE_SIZE)
            val snippetPage = snippetRepository.findByOwnerId(userId, pageable)

            snippetPage.content.forEach { snippet ->
                println("... Solicitando re-validación para snippet ${snippet.id}")
                asyncTaskProducer.requestLinting(
                    snippetId = snippet.id ?: 0L,
                    bucketContainer = snippet.bucketContainer,
                    bucketKey = snippet.bucketKey!!,
                    version = snippet.version,
                    languageId = snippet.language.name, // o snippet.id.toString() si así lo usas
                    userId = userId,
                )
            }

            totalSent += snippetPage.numberOfElements.toLong()
            if (!snippetPage.hasNext()) break
            page++
        }

        println("✅ [Snippet Service] $totalSent snippets enviados a re-validar.")
    }

    /**
     * Dispara el re-formateo para todos los snippets de un usuario.
     */
    fun handleFormattingRulesUpdate(userId: String) {
        println("🔔 [Snippet Service] Recibida actualización de reglas de FORMATO para: $userId")
        println("... Buscando snippets para re-formatear...")

        var page = 0
        var totalSent = 0L
        while (true) {
            val pageable = PageRequest.of(page, PAGE_SIZE)
            val snippetPage = snippetRepository.findByOwnerId(userId, pageable)

            snippetPage.content.forEach { snippet ->
                println("... Solicitando re-formateo para snippet ${snippet.id}")
                asyncTaskProducer.requestFormatting(
                    snippetId = snippet.id ?: 0L,
                    bucketContainer = snippet.bucketContainer,
                    bucketKey = snippet.bucketKey!!,
                    version = snippet.version,
                    languageId = snippet.language.name, // o snippet.id.toString()
                    userId = userId,
                )
            }

            totalSent += snippetPage.numberOfElements.toLong()
            if (!snippetPage.hasNext()) break
            page++
        }

        println("✅ [Snippet Service] $totalSent snippets enviados a re-formatear.")
    }
}
