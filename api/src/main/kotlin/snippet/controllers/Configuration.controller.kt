package snippet.controllers

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import snippet.component.PrintScriptServiceClient
import snippet.dtos.AnalyzerRuleDTO
import snippet.dtos.FormatterRuleDTO
import snippet.dtos.requests.UpdateAnalyzerConfigRequestDTO
import snippet.dtos.requests.UpdateFormatterConfigRequestDTO
import snippet.security.AuthenticatedUserProvider

@RestController
@RequestMapping("/config")
class ConfigurationController(
    private val printScriptServiceClient: PrintScriptServiceClient,
    private val authenticatedUserProvider: AuthenticatedUserProvider,
) {
    @PutMapping("/update/format")
    fun updateFormatterConfig(
        @RequestBody request: UpdateFormatterConfigRequestDTO,
    ): ResponseEntity<List<FormatterRuleDTO>> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        val updatedRules = printScriptServiceClient.updateFormatterConfig(userId, request.rules)
        return ResponseEntity.ok(updatedRules)
    }

    @PutMapping("/update/analyze")
    fun updateAnalyzerConfig(
        @RequestBody request: UpdateAnalyzerConfigRequestDTO,
    ): ResponseEntity<List<AnalyzerRuleDTO>> {
        val userId = authenticatedUserProvider.getCurrentUserId()
        val updatedRules = printScriptServiceClient.updateAnalyzerConfig(userId, request.rules)
        return ResponseEntity.ok(updatedRules)
    }
}
