package snippet.config // Asegúrate que el package sea 'snippet.config'

import io.lettuce.core.ClientOptions
import io.lettuce.core.SocketOptions
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RedisConfig {

    @Bean
    fun lettuceClientConfigurationBuilderCustomizer(): LettuceClientConfigurationBuilderCustomizer =
        LettuceClientConfigurationBuilderCustomizer { clientConfigurationBuilder ->

            println(
                "\n\n✅✅✅ [Snippet Service] ¡SÍ ESTOY ESCANEANDO EL PAQUETE snippet.config Y CARGANDO RedisConfig.kt! ✅✅✅\n\n",
            )

            val socketOptions =
                SocketOptions
                    .builder()
                    .keepAlive(true) // <-- LA VERDADERA SOLUCIÓN
                    .build()

            val clientOptions =
                ClientOptions
                    .builder()
                    .socketOptions(socketOptions)
                    .build()

            clientConfigurationBuilder.clientOptions(clientOptions)
        }
}
