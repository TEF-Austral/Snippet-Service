package snippet.dtos
import common.Language

data class SnippetFilterDTO(
    val ownership: OwnershipFilter = OwnershipFilter.ALL,
    val name: String? = null,
    val language: Language? = null,
    val compliance: ComplianceFilter = ComplianceFilter.ALL,
    val sortBy: SortField = SortField.NAME,
    val sortOrder: SortOrder = SortOrder.ASC,
)

enum class OwnershipFilter {
    OWNED,
    SHARED,
    ALL,
}

enum class ComplianceFilter {
    PENDING,
    FAILED,
    NON_COMPLIANT,
    COMPLIANT,
    ALL,
}

enum class SortField {
    NAME,
    LANGUAGE,
    COMPLIANCE,
}

enum class SortOrder {
    ASC,
    DESC,
}
