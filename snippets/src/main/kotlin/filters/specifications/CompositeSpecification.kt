package filters.specifications

import entity.Snippet

import org.springframework.data.jpa.domain.Specification

class OrSpecification(
    private val specifications: List<SnippetSpecification>,
) : SnippetSpecification {

    constructor(vararg specs: SnippetSpecification) : this(specs.toList())

    override fun toSpecification(): Specification<Snippet> =
        Specification { root, query, cb ->
            val predicates =
                specifications
                    .mapNotNull { spec ->
                        spec.toSpecification().toPredicate(root, query, cb)
                    }.toTypedArray()
            cb.or(*predicates)
        }
}

/**
 * Composite specification that combines multiple specifications using AND logic.
 * Follows the Composite pattern for flexible specification composition.
 * Open/Closed Principle: Can be extended with new specifications without modification.
 */
class AndSpecification(
    private val specifications: List<SnippetSpecification>,
) : SnippetSpecification {

    constructor(vararg specs: SnippetSpecification) : this(specs.toList())

    override fun toSpecification(): Specification<Snippet> =
        Specification { root, query, cb ->
            val predicates =
                specifications
                    .mapNotNull { spec ->
                        spec.toSpecification().toPredicate(root, query, cb)
                    }.toTypedArray()
            cb.and(*predicates)
        }
}
