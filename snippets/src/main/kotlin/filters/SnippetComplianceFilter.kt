package filters

import dtos.requests.ComplianceFilter
import entity.Snippet
import org.springframework.data.jpa.domain.Specification

class SnippetComplianceFilter(
    private val compliance: ComplianceFilter,
) : SnippetFilter {

    override fun apply(spec: Specification<Snippet>): Specification<Snippet> =
        SnippetSpecifications
            .complianceFilter(compliance)
            ?.let { spec.and(it) }
            ?: spec

    override fun isApplicable(): Boolean = compliance != ComplianceFilter.ALL
}
