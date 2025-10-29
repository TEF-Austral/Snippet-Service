package snippet
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@ComponentScan(
    basePackages = [
        "config",
        "controllers",
        "services",
        "repositories",
        "security",
        "component",
        "dtos",
        "entities",
        "exception"
    ]
)
@EnableJpaRepositories(basePackages = ["repositories"])
@EntityScan(basePackages = ["entities"])
class SnippetServiceApplication

fun main(args: Array<String>) {
    runApplication<SnippetServiceApplication>(*args)
}
