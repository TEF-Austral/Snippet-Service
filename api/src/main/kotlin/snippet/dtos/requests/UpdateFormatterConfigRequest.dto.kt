package snippet.dtos.requests

import snippet.dtos.FormatterRuleDTO

data class UpdateFormatterConfigRequestDTO(
    val rules: List<FormatterRuleDTO>,
)
