package snippet.dtos.requests

import snippet.dtos.AnalyzerRuleDTO

data class UpdateAnalyzerConfigRequestDTO(
    val rules: List<AnalyzerRuleDTO>,
)
