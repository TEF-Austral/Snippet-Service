package filters

import entity.Snippet
import org.springframework.data.jpa.domain.Specification

interface SnippetFilter {
    fun apply(spec: Specification<Snippet>): Specification<Snippet>

    fun isApplicable(): Boolean
}
