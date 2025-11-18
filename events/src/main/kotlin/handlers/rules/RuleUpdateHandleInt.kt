package handlers.rules

interface RuleUpdateHandleInt {

    fun canHandle(type: RuleType): Boolean

    fun handle(
        userId: String,
        pageSize: Int = 10,
    )
}
