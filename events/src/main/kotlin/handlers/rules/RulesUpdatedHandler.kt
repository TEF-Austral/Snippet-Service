package handlers.rules

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class RulesUpdatedHandler(
    private val rulesHandlers: List<RuleUpdateHandleInt>,
) {
    private val log = LoggerFactory.getLogger(RulesUpdatedHandler::class.java)

    fun handle(
        ruleType: RuleType,
        userId: String,
    ) {
        log.info("Handling rules update: ruleType=$ruleType, userId=$userId")

        val handler = rulesHandlers.find { it.canHandle(ruleType) }

        if (handler != null) {
            handler.handle(userId)
        } else {
            log.warn("Type of rule not found: ruleType=$ruleType, userId=$userId")
        }
    }
}
