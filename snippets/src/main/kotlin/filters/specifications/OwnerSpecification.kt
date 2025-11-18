package filters.specifications

import entity.Snippet
import org.springframework.data.jpa.domain.Specification

class OwnerSpecification(
    private val ownerId: String,
) : SnippetSpecification {

    override fun toSpecification(): Specification<Snippet> =
        Specification { root, _, cb ->
            cb.equal(root.get<String>("ownerId"), ownerId)
        }
}
