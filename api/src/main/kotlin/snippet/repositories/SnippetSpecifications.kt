package snippet.repositories

import common.Language
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification
import snippet.entities.Snippet

object SnippetSpecifications {

    fun hasOwnerId(ownerId: String): Specification<Snippet> =
        Specification { root, _, cb ->
            cb.equal(root.get<String>("ownerId"), ownerId)
        }

    fun hasIdIn(ids: List<Long>): Specification<Snippet> =
        Specification { root, _, cb ->
            if (ids.isEmpty()) {
                cb.disjunction()
            } else {
                root.get<Long>("id").`in`(ids)
            }
        }

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
