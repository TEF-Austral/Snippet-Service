package dtos.requests

import dtos.types.ComplianceFilter
import dtos.types.Language
import dtos.types.OwnershipFilter
import dtos.types.SortField
import dtos.types.SortOrder

data class SnippetFilterDTO(
    val ownership: OwnershipFilter = OwnershipFilter.ALL,
    val name: String? = null,
    val language: Language? = null,
    val compliance: ComplianceFilter = ComplianceFilter.ALL,
    val sortBy: SortField = SortField.NAME,
    val sortOrder: SortOrder = SortOrder.ASC,
)
