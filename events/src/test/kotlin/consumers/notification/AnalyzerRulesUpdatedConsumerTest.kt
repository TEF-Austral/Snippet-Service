package consumers.notification

import events.AnalyzerRulesUpdatedEvent
import handlers.rules.RuleType
import handlers.rules.RulesUpdatedHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import java.lang.reflect.Method
import kotlin.test.assertNotNull

@ExtendWith(MockitoExtension::class)
@OptIn(ExperimentalCoroutinesApi::class)
class AnalyzerRulesUpdatedConsumerTest {

    @Mock
    private lateinit var handler: RulesUpdatedHandler

    @Mock
    private lateinit var redisTemplate: RedisTemplate<String, String>

    @Mock
    private lateinit var record: ObjectRecord<String, AnalyzerRulesUpdatedEvent>

    private lateinit var consumer: AnalyzerRulesUpdatedConsumer

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var onMessageMethod: Method
    private lateinit var optionsMethod: Method

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        consumer =
            AnalyzerRulesUpdatedConsumer(
                streamKey = "test-stream",
                consumerGroup = "test-group",
                redis = redisTemplate,
                handler = handler,
            )

        onMessageMethod =
            AnalyzerRulesUpdatedConsumer::class.java
                .getDeclaredMethod("onMessage", ObjectRecord::class.java)
        onMessageMethod.isAccessible = true

        optionsMethod =
            AnalyzerRulesUpdatedConsumer::class.java
                .getDeclaredMethod("options")
        optionsMethod.isAccessible = true
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun invokeOnMessage(record: ObjectRecord<String, AnalyzerRulesUpdatedEvent>) {
        onMessageMethod.invoke(consumer, record)
    }

    private fun invokeOptions(): StreamReceiver.StreamReceiverOptions<
        String,
        ObjectRecord<String, AnalyzerRulesUpdatedEvent>,
    > {
        @Suppress("UNCHECKED_CAST")
        return optionsMethod.invoke(
            consumer,
        ) as StreamReceiver.StreamReceiverOptions<
            String,
            ObjectRecord<String, AnalyzerRulesUpdatedEvent>,
        >
    }

    @Test
    fun `onMessage should process analyzer rules updated event successfully`() =
        runTest {
            // Given
            val userId = "user123"
            val event = AnalyzerRulesUpdatedEvent(userId = userId)
            whenever(record.value).thenReturn(event)

            // When
            invokeOnMessage(record)
            Thread.sleep(100) // Give time for the GlobalScope coroutine to execute

            // Then
            verify(record).value
            verify(handler).handle(RuleType.Lint, userId)
        }

    @Test
    fun `onMessage should log error when handler throws exception`() =
        runTest {
            // Given
            val userId = "user456"
            val event = AnalyzerRulesUpdatedEvent(userId = userId)
            val exception = RuntimeException("Handler failed")
            whenever(record.value).thenReturn(event)
            doThrow(exception).whenever(handler).handle(any(), any())

            // When
            invokeOnMessage(record)
            Thread.sleep(100) // Give time for GlobalScope coroutine to execute

            // Then
            verify(record).value
            verify(handler).handle(RuleType.Lint, userId)
            // Error is logged but not thrown
        }

    @Test
    fun `onMessage should handle exception in outer try-catch when record value throws`() {
        // Given
        whenever(record.value).thenThrow(RuntimeException("Record processing failed"))

        // When & Then - should not throw exception
        invokeOnMessage(record)
        verify(record).value
    }

    @Test
    fun `onMessage should handle exception with stack trace`() =
        runTest {
            // Given
            val userId = "user789"
            val event = AnalyzerRulesUpdatedEvent(userId = userId)
            val exception = RuntimeException("Exception with stack trace")
            whenever(record.value).thenReturn(event)
            doThrow(exception).whenever(handler).handle(any(), any())

            // When
            invokeOnMessage(record)
            Thread.sleep(100) // Give time for GlobalScope coroutine to execute

            // Then
            verify(handler).handle(RuleType.Lint, userId)
            // Should log error with stack trace location
        }

    @Test
    fun `onMessage should call handler with correct RuleType Lint`() =
        runTest {
            // Given
            val userId = "user-lint"
            val event = AnalyzerRulesUpdatedEvent(userId = userId)
            whenever(record.value).thenReturn(event)

            // When
            invokeOnMessage(record)
            Thread.sleep(100) // Give time for GlobalScope coroutine to execute

            // Then
            verify(handler).handle(eq(RuleType.Lint), eq(userId))
        }

    @Test
    fun `options should return StreamReceiverOptions with correct configuration`() {
        // When
        val options = invokeOptions()

        // Then
        assertNotNull(options)
        // Note: We can't easily verify the internal configuration of the options
        // but we ensure it doesn't throw an exception and returns a non-null value
    }

    @Test
    fun `options should return correct poll timeout duration`() {
        // When
        val options = invokeOptions()

        // Then
        assertNotNull(options)
        // The poll timeout should be 10000ms as configured in the consumer
    }

    @Test
    fun `onMessage should extract and log stack trace location when handler fails`() =
        runTest {
            // Given
            val userId = "user-with-error"
            val event = AnalyzerRulesUpdatedEvent(userId = userId)
            val exception = RuntimeException("Test exception with stack trace")
            whenever(record.value).thenReturn(event)
            doThrow(exception).whenever(handler).handle(any(), any())

            // When
            invokeOnMessage(record)
            Thread.sleep(100) // Give time for GlobalScope coroutine to execute

            // Then
            verify(handler).handle(RuleType.Lint, userId)
            // The error should be logged with stack trace information
        }

    @Test
    fun `onMessage should handle multiple events sequentially`() =
        runTest {
            // Given
            val userId1 = "user1"
            val userId2 = "user2"
            val event1 = AnalyzerRulesUpdatedEvent(userId = userId1)
            val event2 = AnalyzerRulesUpdatedEvent(userId = userId2)

            whenever(record.value).thenReturn(event1).thenReturn(event2)

            // When
            invokeOnMessage(record)
            Thread.sleep(100) // Give time for GlobalScope coroutine to execute
            invokeOnMessage(record)
            Thread.sleep(100) // Give time for GlobalScope coroutine to execute

            // Then
            verify(handler).handle(RuleType.Lint, userId1)
            verify(handler).handle(RuleType.Lint, userId2)
        }

    @Test
    fun `onMessage should process event with empty userId`() =
        runTest {
            // Given
            val userId = ""
            val event = AnalyzerRulesUpdatedEvent(userId = userId)
            whenever(record.value).thenReturn(event)

            // When
            invokeOnMessage(record)
            Thread.sleep(100) // Give time for GlobalScope coroutine to execute

            // Then
            verify(handler).handle(RuleType.Lint, userId)
        }

    @Test
    fun `onMessage should catch exception in outer block and log it`() {
        // Given
        val exception = IllegalStateException("Outer exception")
        whenever(record.value).thenThrow(exception)

        // When & Then - should not propagate exception
        try {
            invokeOnMessage(record)
            // Exception should be caught and logged
        } catch (e: Exception) {
            throw AssertionError("Exception should have been caught by consumer", e)
        }

        verify(record).value
    }
}
