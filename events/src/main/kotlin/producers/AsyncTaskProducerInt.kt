package producers

import AsyncTaskRequestContext
import producers.strategy.TaskType

interface AsyncTaskProducerInt {

    fun request(
        type: TaskType,
        context: AsyncTaskRequestContext,
    ): String
}
