package filters

import dtos.requests.OwnershipFilter
import entity.Snippet
import org.springframework.data.jpa.domain.Specification

class SnippetOwnershipFilter(
    private val requesterId: String,
    private val sharedSnippetIds: List<Long>,
    private val ownership: OwnershipFilter,
) : SnippetFilter {

    override fun apply(spec: Specification<Snippet>): Specification<Snippet> =
        spec.and(SnippetSpecifications.ownershipFilter(requesterId, sharedSnippetIds, ownership))

    override fun isApplicable(): Boolean = true
}
