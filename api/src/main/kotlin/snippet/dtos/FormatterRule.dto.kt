package snippet.dtos

data class FormatterRuleDTO(
    val id: Long?,
    val name: String,
    val isActive: Boolean,
    val value: String?,
)
