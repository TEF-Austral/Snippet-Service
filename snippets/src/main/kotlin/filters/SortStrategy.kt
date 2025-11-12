package filters

import dtos.requests.SortField
import dtos.requests.SortOrder
import org.springframework.data.domain.Sort

interface SortStrategy {
    fun createSort(
        sortBy: SortField,
        sortOrder: SortOrder,
    ): Sort
}

class SnippetSortStrategy : SortStrategy {

    override fun createSort(
        sortBy: SortField,
        sortOrder: SortOrder,
    ): Sort {
        val fieldName = mapSortField(sortBy)
        val direction = mapSortDirection(sortOrder)
        return Sort.by(direction, fieldName)
    }

    private fun mapSortField(sortBy: SortField): String =
        when (sortBy) {
            SortField.NAME -> "name"
            SortField.LANGUAGE -> "language"
            SortField.COMPLIANCE -> "complianceStatus"
        }

    private fun mapSortDirection(sortOrder: SortOrder): Sort.Direction =
        when (sortOrder) {
            SortOrder.ASC -> Sort.Direction.ASC
            SortOrder.DESC -> Sort.Direction.DESC
        }
}
