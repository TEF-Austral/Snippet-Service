package filters

import dtos.requests.ComplianceFilter
import dtos.requests.OwnershipFilter
import dtos.types.Language
import entity.Snippet
import filters.specifications.ComplianceSpecificationFactory
import filters.specifications.LanguageSpecification
import filters.specifications.NameContainsSpecification
import filters.specifications.OwnershipSpecificationBuilder
import org.springframework.data.jpa.domain.Specification

object SnippetSpecifications {

    fun nameContains(name: String): Specification<Snippet> =
        NameContainsSpecification(name).toSpecification()

    fun hasLanguage(language: Language): Specification<Snippet> =
        LanguageSpecification(language).toSpecification()

    fun complianceFilter(compliance: ComplianceFilter): Specification<Snippet>? =
        ComplianceSpecificationFactory.createSpecification(compliance)

    fun ownershipFilter(
        ownerId: String,
        sharedIds: List<Long>,
        ownership: OwnershipFilter,
    ): Specification<Snippet> = OwnershipSpecificationBuilder(ownerId, sharedIds).build(ownership)
}
