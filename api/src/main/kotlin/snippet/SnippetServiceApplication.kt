package snippet

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["snippet", "events"])
class SnippetServiceApplication

fun main(args: Array<String>) {
    runApplication<SnippetServiceApplication>(*args)
}
// ayayay
