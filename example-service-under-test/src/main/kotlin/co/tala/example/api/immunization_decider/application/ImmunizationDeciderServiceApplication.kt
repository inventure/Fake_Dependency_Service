package co.tala.example.api.immunization_decider.application

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication

@EntityScan("co.tala.example.api.*")
@SpringBootApplication(scanBasePackages = ["co.tala.example.api.*"])
class ImmunizationDeciderServiceApplication

fun main(args: Array<String>) {
    runApplication<ImmunizationDeciderServiceApplication>(*args)
}
