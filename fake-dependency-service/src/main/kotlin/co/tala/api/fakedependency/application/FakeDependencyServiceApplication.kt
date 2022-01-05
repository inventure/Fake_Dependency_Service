package co.tala.api.fakedependency.application

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["co.tala.api.*"])
class FakeDependencyServiceApplication

fun main(args: Array<String>) {
    runApplication<FakeDependencyServiceApplication>(*args)
}
