package filters
import dtos.requests.SnippetFilterDTO
import org.springframework.stereotype.Component

@Component
class SnippetFilterFactory {

    fun createFilters(
        filterDTO: SnippetFilterDTO,
        requesterId: String,
        sharedSnippetIds: List<Long>,
    ): List<SnippetFilter> =
        listOf(
            SnippetOwnershipFilter(requesterId, sharedSnippetIds, filterDTO.ownership),
            SnippetNameFilter(filterDTO.name),
            SnippetLanguageFilter(filterDTO.language),
            SnippetComplianceFilter(filterDTO.compliance),
        )
}
