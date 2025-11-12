package api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackages = [
        "events", "common", "security",
        "authorization", "consumers", "handlers",
        "config", "producers", "api", "assets",
        "repositories", "component", "component",
    ],
)
class SnippetServiceApplication

fun main(args: Array<String>) {
    runApplication<SnippetServiceApplication>(*args)
}
