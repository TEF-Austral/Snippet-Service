package snippet

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["snippet", "events"])
class SnippetServiceApplication

fun main(args: Array<String>) {
    runApplication<SnippetServiceApplication>(*args)
}
