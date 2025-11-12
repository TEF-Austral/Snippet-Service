package config

import io.lettuce.core.ClientOptions
import io.lettuce.core.SocketOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory

@Configuration
class RedisConfig {

    @Bean
    fun lettuceConnectionFactory(
        @Value("\${spring.data.redis.host}") host: String,
        @Value("\${spring.data.redis.port}") port: Int,
    ): LettuceConnectionFactory {
        val socketOptions =
            SocketOptions
                .builder()
                .keepAlive(true)
                .build()

        val clientOptions =
            ClientOptions
                .builder()
                .socketOptions(socketOptions)
                .build()

        val clientConfig =
            LettuceClientConfiguration
                .builder()
                .clientOptions(clientOptions)
                .build()

        val serverConfig = RedisStandaloneConfiguration(host, port)

        return LettuceConnectionFactory(serverConfig, clientConfig)
    }
}
