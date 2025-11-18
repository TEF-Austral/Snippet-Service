package filters.specifications

import entity.Snippet

import org.springframework.data.jpa.domain.Specification

class OrSpecification(
    private val specifications: List<SnippetSpecification>,
) : SnippetSpecification {

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

class AndSpecification(
    private val specifications: List<SnippetSpecification>,
) : SnippetSpecification {

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
