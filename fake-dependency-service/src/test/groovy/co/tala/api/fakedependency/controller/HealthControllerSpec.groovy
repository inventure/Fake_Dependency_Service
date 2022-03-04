package co.tala.api.fakedependency.controller

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.Status
import spock.lang.Specification

class HealthControllerSpec extends Specification {
    def "health should be up"(){
        given:
            HealthController sut = new HealthController()

        when:
            Health result = sut.health()

        then:
            result.status == Status.UP
    }
}
