package filters

import entity.Snippet
import org.springframework.data.jpa.domain.Specification

class SnippetNameFilter(
    private val name: String?,
) : SnippetFilter {

    override fun apply(spec: Specification<Snippet>): Specification<Snippet> =
        name
            ?.let { SnippetSpecifications.nameContains(it) }
            ?.let { spec.and(it) }
            ?: spec

    override fun isApplicable(): Boolean = !name.isNullOrBlank()
}
