package config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    @Order(1)
    fun actuatorSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher("/actuator/**")
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth.anyRequest().permitAll()
            }

        return http.build()
    }

    @Bean
    @Order(2)
    fun apiSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests { auth ->
                auth.anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { }
            }

        return http.build()
    }
}
