package snippet.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RedisConfig {

    @Bean
    fun objectMapper(): ObjectMapper =
        ObjectMapper().apply {
            registerKotlinModule()
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        }

    @Bean
    fun redisTemplate(
        connectionFactory: RedisConnectionFactory,
        objectMapper: ObjectMapper,
    ): RedisTemplate<String, String> {
        val template = RedisTemplate<String, String>()
        template.connectionFactory = connectionFactory

        val stringSerializer = StringRedisSerializer()
        val jackson2JsonRedisSerializer = Jackson2JsonRedisSerializer(objectMapper, Any::class.java)

        template.keySerializer = stringSerializer
        template.valueSerializer = jackson2JsonRedisSerializer
        template.hashKeySerializer = stringSerializer
        template.hashValueSerializer = jackson2JsonRedisSerializer

        template.afterPropertiesSet()
        return template
    }

    @Bean
    fun reactiveRedisTemplate(
        connectionFactory: ReactiveRedisConnectionFactory,
        objectMapper: ObjectMapper,
    ): ReactiveRedisTemplate<String, String> {
        val stringSerializer = StringRedisSerializer()
        val jackson2JsonRedisSerializer = Jackson2JsonRedisSerializer(objectMapper, Any::class.java)

        val serializationContext =
            RedisSerializationContext
                .newSerializationContext<String, String>(stringSerializer)
                .key(stringSerializer)
                .value(jackson2JsonRedisSerializer)
                .hashKey(stringSerializer)
                .hashValue(jackson2JsonRedisSerializer)
                .build()

        return ReactiveRedisTemplate(connectionFactory, serializationContext)
    }
}
