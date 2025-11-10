package snippet.config

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
        println(
            "\n\n✅✅✅ [Snippet Service] ¡CARGANDO NUEVA CONFIG UNIFICADA DE REDIS CON keepAlive! ✅✅✅\n\n",
        )

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

        // Construye la configuración del cliente Lettuce
        val clientConfig =
            LettuceClientConfiguration
                .builder()
                .clientOptions(clientOptions)
                .build()

        // Configuración del servidor Redis
        val serverConfig = RedisStandaloneConfiguration(host, port)

        // Crea la fábrica de conexiones que será usada por RedisTemplate (bloqueante)
        // Y por ReactiveRedisTemplate (reactivo)
        return LettuceConnectionFactory(serverConfig, clientConfig)
    }
}
