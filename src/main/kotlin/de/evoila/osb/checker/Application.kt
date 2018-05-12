package de.evoila.osb.checker

import de.evoila.osb.checker.config.Configuration
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication


@EnableAutoConfiguration
@SpringBootApplication
class Application(
    val configuration: Configuration
)

fun main(args: Array<String>) {
  SpringApplication.run(Application::class.java)
}