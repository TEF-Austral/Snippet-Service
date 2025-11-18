package filters.specifications

import entity.Snippet
import org.springframework.data.jpa.domain.Specification

interface SnippetSpecification {
    fun toSpecification(): Specification<Snippet>
}
