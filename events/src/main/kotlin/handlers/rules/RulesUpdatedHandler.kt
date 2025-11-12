package handlers.rules

import org.springframework.stereotype.Service

@Service
class RulesUpdatedHandler(
    private val rulesHandlers: List<RuleUpdateHandleInt>,
) {

    fun handle(
        ruleType: RuleType,
        userId: String,
    ) {
        val handler = rulesHandlers.find { it.canHandle(ruleType) }

        if (handler != null) {
            handler.handle(userId)
        } else {
            println("[Snippet Service]: Type of rule not found: $ruleType")
        }
    }
}
