package snippet

import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@ComponentScan(basePackages = ["snippet", "events"])
class SnippetServiceApplication

fun main(args: Array<String>) {
    runApplication<SnippetServiceApplication>(*args)
}
