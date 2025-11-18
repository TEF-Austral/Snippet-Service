package filters.specifications

import entity.Snippet
import org.springframework.data.jpa.domain.Specification

class NameContainsSpecification(
    private val name: String,
) : SnippetSpecification {

    override fun toSpecification(): Specification<Snippet> =
        Specification { root, _, cb ->
            cb.like(cb.lower(root.get("name")), "%${name.lowercase()}%")
        }
}
