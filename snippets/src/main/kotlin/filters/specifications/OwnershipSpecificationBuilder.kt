package filters.specifications

import dtos.requests.OwnershipFilter
import entity.Snippet
import org.springframework.data.jpa.domain.Specification

class OwnershipSpecificationBuilder(
    private val ownerId: String,
    private val sharedIds: List<Long>,
) {

    fun build(ownership: OwnershipFilter): Specification<Snippet> =
        when (ownership) {
            OwnershipFilter.OWNED -> createOwnedSpecification()
            OwnershipFilter.SHARED -> createSharedSpecification()
            OwnershipFilter.ALL -> createAllSpecification()
        }.toSpecification()

    private fun createOwnedSpecification(): SnippetSpecification = OwnerSpecification(ownerId)

    private fun createSharedSpecification(): SnippetSpecification = SharedSpecification(sharedIds)

    private fun createAllSpecification(): SnippetSpecification {
        val specifications = mutableListOf<SnippetSpecification>()
        specifications.add(OwnerSpecification(ownerId))

        if (sharedIds.isNotEmpty()) {
            specifications.add(SharedSpecification(sharedIds))
        }

        return OrSpecification(specifications)
    }
}
