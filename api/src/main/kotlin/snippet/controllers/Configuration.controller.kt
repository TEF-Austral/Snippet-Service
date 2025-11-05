package snippet.controllers

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import snippet.component.PrintScriptServiceClient
import snippet.dtos.AnalyzerRuleDTO
import snippet.dtos.FormatterRuleDTO
import snippet.dtos.requests.UpdateAnalyzerConfigRequestDTO
import snippet.dtos.requests.UpdateFormatterConfigRequestDTO

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

    @PutMapping("/update/format")
    fun updateFormatterConfig(
        @RequestBody request: UpdateFormatterConfigRequestDTO,
    ): ResponseEntity<List<FormatterRuleDTO>> {
        val updatedRules = printScriptServiceClient.updateFormatterConfig(request.rules)
        return ResponseEntity.ok(updatedRules)
    }

    @GetMapping("/analyze")
    fun getAnalyzerConfig(): ResponseEntity<List<AnalyzerRuleDTO>> {
        val rules = printScriptServiceClient.getAnalyzerConfig()
        return ResponseEntity.ok(rules)
    }

    @PutMapping("/update/analyze")
    fun updateAnalyzerConfig(
        @RequestBody request: UpdateAnalyzerConfigRequestDTO,
    ): ResponseEntity<List<AnalyzerRuleDTO>> {
        val updatedRules = printScriptServiceClient.updateAnalyzerConfig(request.rules)
        return ResponseEntity.ok(updatedRules)
    }
}
