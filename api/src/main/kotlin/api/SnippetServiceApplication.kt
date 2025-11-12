package api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(
    scanBasePackages = [
        "events", "common", "security",
        "authorization", "consumers", "handlers",
        "config", "producers", "api", "assets",
        "repositories", "component", "filters",
        "controllers", "services",
        // Módulo 'api'
        "api",

        // Módulo 'events'
        "config",
        "consumers",
        "events",
        "handlers",
        "producers",
        "requests",

        // Módulo 'common'
        "repositories",

        // Módulos 'printscript' y 'assets'
        "component",

        // Módulos 'printscript' y 'snippets'
        "services",

        // Módulo 'security'
        "security",

        // Módulo 'authorization'
        "authorization",

        // Módulo 'snippets'
        "controllers",
        "filters",
    ],
)
@EnableJpaRepositories("repositories")
@EntityScan("entity")
class SnippetServiceApplication

fun main(args: Array<String>) {
    runApplication<SnippetServiceApplication>(*args)
}
