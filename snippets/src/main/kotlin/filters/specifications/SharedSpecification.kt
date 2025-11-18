package filters.specifications

import entity.Snippet
import org.springframework.data.jpa.domain.Specification

class SharedSpecification(
    private val sharedIds: List<Long>,
) : SnippetSpecification {

    override fun toSpecification(): Specification<Snippet> =
        if (sharedIds.isEmpty()) {
            Specification { root, _, cb -> cb.disjunction() }
        } else {
            Specification { root, _, _ -> root.get<Long>("id").`in`(sharedIds) }
        }
}
