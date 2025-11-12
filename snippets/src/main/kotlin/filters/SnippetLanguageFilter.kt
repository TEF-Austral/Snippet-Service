package filters

import dtos.types.Language
import entity.Snippet
import org.springframework.data.jpa.domain.Specification

class SnippetLanguageFilter(
    private val language: Language?,
) : SnippetFilter {

    override fun apply(spec: Specification<Snippet>): Specification<Snippet> =
        language
            ?.let { SnippetSpecifications.hasLanguage(it) }
            ?.let { spec.and(it) }
            ?: spec

    override fun isApplicable(): Boolean = language != null
}
