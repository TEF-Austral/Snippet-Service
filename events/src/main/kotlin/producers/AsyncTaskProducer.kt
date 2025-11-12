package producers

import AsyncTaskRequestContext
import org.springframework.stereotype.Component
import producers.strategy.AsyncTaskStrategy
import producers.strategy.TaskType

@Component
class AsyncTaskProducer(
    private val strategies: List<AsyncTaskStrategy>,
) : AsyncTaskProducerInt {

    override fun request(
        type: TaskType,
        context: AsyncTaskRequestContext,
    ): String {
        for (strategy in strategies) {
            if (strategy.canHandle(type)) {
                return strategy.submit(context)
            }
        }
        throw IllegalArgumentException("No strategy found for task type: $type")
    }
}
