package filters.specifications

import dtos.types.ComplianceStatus
import entity.Snippet
import org.springframework.data.jpa.domain.Specification

/**
 * Specification for filtering snippets by compliance status.
 * Single Responsibility: Only handles compliance status filtering logic.
 */
class ComplianceSpecification(
    private val status: ComplianceStatus,
) : SnippetSpecification {

    override fun toSpecification(): Specification<Snippet> =
        Specification { root, _, cb ->
            cb.equal(root.get<ComplianceStatus>("complianceStatus"), status)
        }
}
