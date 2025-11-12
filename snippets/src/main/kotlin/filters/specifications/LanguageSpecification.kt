package filters.specifications

import dtos.types.Language
import entity.Snippet
import org.springframework.data.jpa.domain.Specification

class LanguageSpecification(
    private val language: Language,
) : SnippetSpecification {

    override fun toSpecification(): Specification<Snippet> =
        Specification { root, _, cb ->
            cb.equal(root.get<Language>("language"), language)
        }
}
