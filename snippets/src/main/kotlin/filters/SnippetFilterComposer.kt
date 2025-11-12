package filters

import entity.Snippet
import org.springframework.data.jpa.domain.Specification

class SnippetFilterComposer {

    private val filters = mutableListOf<SnippetFilter>()

    fun addFilter(filter: SnippetFilter): SnippetFilterComposer {
        filters.add(filter)
        return this
    }

    fun build(): Specification<Snippet> {
        var spec: Specification<Snippet> = Specification.where(null)

        filters
            .filter { it.isApplicable() }
            .forEach { filter ->
                spec = filter.apply(spec)
            }

        return spec
    }
}
