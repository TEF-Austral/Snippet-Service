package filters.specifications

import common.dtos.types.ComplianceStatus
import dtos.requests.ComplianceFilter
import entity.Snippet
import org.springframework.data.jpa.domain.Specification

/**
 * Factory for creating compliance-related specifications.
 * Follows the Factory pattern and Single Responsibility Principle.
 * Open/Closed Principle: Easy to add new compliance filter types without modifying existing code.
 */
object ComplianceSpecificationFactory {

    fun createSpecification(compliance: ComplianceFilter): Specification<Snippet>? =
        when (compliance) {
            ComplianceFilter.PENDING ->
                ComplianceSpecification(
                    ComplianceStatus.PENDING,
                ).toSpecification()
            ComplianceFilter.FAILED ->
                ComplianceSpecification(
                    ComplianceStatus.FAILED,
                ).toSpecification()
            ComplianceFilter.NON_COMPLIANT ->
                ComplianceSpecification(
                    ComplianceStatus.NON_COMPLIANT,
                ).toSpecification()
            ComplianceFilter.COMPLIANT ->
                ComplianceSpecification(
                    ComplianceStatus.COMPLIANT,
                ).toSpecification()
            ComplianceFilter.ALL -> null
        }
}
