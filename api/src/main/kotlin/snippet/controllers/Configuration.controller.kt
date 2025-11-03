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
import snippet.security.AuthenticatedUserProvider

@RestController
@RequestMapping("/config")
class ConfigurationController(
    private val printScriptServiceClient: PrintScriptServiceClient,
    private val authenticatedUserProvider: AuthenticatedUserProvider,
) {

    @GetMapping("/format")
    fun getFormatterConfig(): ResponseEntity<List<FormatterRuleDTO>> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        val rules = printScriptServiceClient.getFormatterConfig(userId)
        return ResponseEntity.ok(rules)
    }

    @PutMapping("/format")
    fun updateFormatterConfig(
        @RequestBody request: UpdateFormatterConfigRequest,
    ): ResponseEntity<List<FormatterRuleDTO>> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        val updatedRules = printScriptServiceClient.updateFormatterConfig(userId, request.rules)
        return ResponseEntity.ok(updatedRules)
    }

    @GetMapping("/analyze")
    fun getAnalyzerConfig(): ResponseEntity<List<AnalyzerRuleDTO>> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        val rules = printScriptServiceClient.getAnalyzerConfig(userId)
        return ResponseEntity.ok(rules)
    }

    @PutMapping("/analyze")
    fun updateAnalyzerConfig(
        @RequestBody request: UpdateAnalyzerConfigRequest,
    ): ResponseEntity<List<AnalyzerRuleDTO>> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        val updatedRules = printScriptServiceClient.updateAnalyzerConfig(userId, request.rules)
        return ResponseEntity.ok(updatedRules)
    }
}

data class UpdateFormatterConfigRequest(
    val rules: List<FormatterRuleDTO>,
)

data class UpdateAnalyzerConfigRequest(
    val rules: List<AnalyzerRuleDTO>,
)