package repositories

import common.Language
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification
import common.entities.ComplianceStatus
import common.entities.Snippet

object SnippetSpecifications {

    fun nameContains(name: String?): Specification<Snippet>? =
        name?.let {
            Specification { root, _, cb ->
                cb.like(cb.lower(root.get("name")), "%${it.lowercase()}%")
            }
        }

    fun hasLanguage(language: Language?): Specification<Snippet>? =
        language?.let {
            Specification { root, _, cb ->
                cb.equal(root.get<Language>("language"), it)
            }
        }

    fun complianceFilter(compliance: String): Specification<Snippet>? =
        when (compliance.uppercase()) {
            "PENDING" ->
                Specification { root, _, cb ->
                    cb.equal(
                        root.get<ComplianceStatus>("complianceStatus"),
                        ComplianceStatus.PENDING,
                    )
                }
            "FAILED" ->
                Specification { root, _, cb ->
                    cb.equal(
                        root.get<ComplianceStatus>("complianceStatus"),
                        ComplianceStatus.FAILED,
                    )
                }
            "NON_COMPLIANT" ->
                Specification { root, _, cb ->
                    cb.equal(
                        root.get<ComplianceStatus>("complianceStatus"),
                        ComplianceStatus.NON_COMPLIANT,
                    )
                }
            "COMPLIANT" ->
                Specification { root, _, cb ->
                    cb.equal(
                        root.get<ComplianceStatus>("complianceStatus"),
                        ComplianceStatus.COMPLIANT,
                    )
                }
            "ALL" -> null
            else -> null
        }

    fun ownershipFilter(
        ownerId: String,
        sharedIds: List<Long>,
        ownership: String,
    ): Specification<Snippet> =
        Specification { root, _, cb ->
            when (ownership.uppercase()) {
                "OWNED" -> cb.equal(root.get<String>("ownerId"), ownerId)
                "SHARED" -> {
                    if (sharedIds.isEmpty()) {
                        cb.disjunction()
                    } else {
                        root.get<Long>("id").`in`(sharedIds)
                    }
                }
                "ALL" -> {
                    val predicates = mutableListOf<Predicate>()
                    predicates.add(cb.equal(root.get<String>("ownerId"), ownerId))
                    if (sharedIds.isNotEmpty()) {
                        predicates.add(root.get<Long>("id").`in`(sharedIds))
                    }
                    cb.or(*predicates.toTypedArray())
                }
                else -> cb.conjunction()
            }
        }
}
