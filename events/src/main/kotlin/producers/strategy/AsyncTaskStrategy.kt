package producers.strategy

import AsyncTaskRequestContext

interface AsyncTaskStrategy {
    fun canHandle(type: TaskType): Boolean

    fun submit(context: AsyncTaskRequestContext): String
}
