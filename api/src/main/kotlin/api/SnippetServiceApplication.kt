package api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

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
class SnippetServiceApplication

fun main(args: Array<String>) {
    runApplication<SnippetServiceApplication>(*args)
}
