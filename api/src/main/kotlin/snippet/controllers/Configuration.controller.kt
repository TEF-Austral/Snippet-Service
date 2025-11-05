package snippet.controllers

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import snippet.component.AnalyzerRuleDTO
import snippet.component.FormatterRuleDTO
import snippet.component.PrintScriptServiceClient

@RestController
@RequestMapping("/config")
class ConfigurationController(
    private val printScriptServiceClient: PrintScriptServiceClient,
) {

    @GetMapping("/format")
    fun getFormatterConfig(): ResponseEntity<List<FormatterRuleDTO>> {
        val rules = printScriptServiceClient.getFormatterConfig()
        return ResponseEntity.ok(rules)
    }

    @PutMapping("/format")
    fun updateFormatterConfig(
        @RequestBody request: UpdateFormatterConfigRequest,
    ): ResponseEntity<List<FormatterRuleDTO>> {
        val updatedRules = printScriptServiceClient.updateFormatterConfig(request.rules)
        return ResponseEntity.ok(updatedRules)
    }

    @GetMapping("/analyze")
    fun getAnalyzerConfig(): ResponseEntity<List<AnalyzerRuleDTO>> {
        val rules = printScriptServiceClient.getAnalyzerConfig()
        return ResponseEntity.ok(rules)
    }

    @PutMapping("/analyze")
    fun updateAnalyzerConfig(
        @RequestBody request: UpdateAnalyzerConfigRequest,
    ): ResponseEntity<List<AnalyzerRuleDTO>> {
        val updatedRules = printScriptServiceClient.updateAnalyzerConfig(request.rules)
        return ResponseEntity.ok(updatedRules)
    }
}

data class UpdateFormatterConfigRequest(
    val rules: List<FormatterRuleDTO>,
)

data class UpdateAnalyzerConfigRequest(
    val rules: List<AnalyzerRuleDTO>,
)
