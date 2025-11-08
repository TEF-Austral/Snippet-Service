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
        private const val PAGE_SIZE = 10 // Reducir de 100 a 10
    }

    /**
     * Dispara el re-linting (validación) para todos los snippets de un usuario.
     */
    fun handleAnalyzerRulesUpdate(userId: String) {
        try {
            println(
                "🔔 [Snippet Service] Recibida actualización de reglas de LINTING para: $userId",
            )
            println("... Buscando snippets para re-validar...")

            var page = 0
            var totalSent = 0L
            while (true) {
                val pageable = PageRequest.of(page, PAGE_SIZE)
                val snippetPage = snippetRepository.findByOwnerId(userId, pageable)

                snippetPage.content.forEach { snippet ->
                    try {
                        println("... Solicitando re-validación para snippet ${snippet.id}")
                        asyncTaskProducer.requestLinting(
                            snippetId = snippet.id ?: 0L,
                            bucketContainer = snippet.bucketContainer,
                            bucketKey = snippet.bucketKey!!,
                            version = snippet.version,
                            languageId = snippet.language.name,
                            userId = userId,
                        )

                        println("📤 Request enviado con snippetID: ${snippet.id}")
                    } catch (e: Exception) {
                        println(
                            "❌ Error enviando validación para snippet ${snippet.id}: ${e.message}",
                        )
                        println("❌❌❌ EXCEPCIÓN CAPTURADA: ${e.javaClass.simpleName} - ${e.message}")
                        e.printStackTrace()
                    }
                }

                totalSent += snippetPage.numberOfElements.toLong()

                if (!snippetPage.hasNext()) break
                page++
            }

            println("✅ [Snippet Service] $totalSent snippets enviados a re-validar.")
        } catch (e: Exception) {
            println(
                "❌ [Snippet Service] Error crítico procesando actualización de reglas de linting para usuario $userId: ${e.message}",
            )
            e.printStackTrace()
        }
    }

    /**
     * Dispara el re-formateo para todos los snippets de un usuario.
     */
    fun handleFormattingRulesUpdate(userId: String) {
        try {
            println(
                "🔔 [Snippet Service] Recibida actualización de reglas de FORMATO para: $userId",
            )

            var page = 0
            var totalSent = 0L

            while (true) {
                val pageable = PageRequest.of(page, PAGE_SIZE)
                val snippetPage = snippetRepository.findByOwnerId(userId, pageable)

                snippetPage.content.forEach { snippet ->
                    try {
                        println("... Solicitando re-formateo para snippet ${snippet.id}")
                        asyncTaskProducer.requestFormatting(
                            snippetId = snippet.id ?: 0L,
                            bucketContainer = snippet.bucketContainer,
                            bucketKey = snippet.bucketKey!!,
                            version = snippet.version,
                            languageId = snippet.language.name,
                            userId = userId,
                        )
                    } catch (e: Exception) {
                        println("❌❌❌ EXCEPCIÓN CAPTURADA: ${e.javaClass.simpleName} - ${e.message}")
                        e.printStackTrace()
                    }
                }

                totalSent += snippetPage.numberOfElements.toLong()

                if (!snippetPage.hasNext()) break

                page++
            }

            println("✅ [Snippet Service] $totalSent snippets enviados a re-formatear.")
        } catch (e: Exception) {
            println(
                "❌ [Snippet Service] Error crítico procesando actualización de reglas de formato para usuario $userId: ${e.message}",
            )
            e.printStackTrace()
        }
    }
}
