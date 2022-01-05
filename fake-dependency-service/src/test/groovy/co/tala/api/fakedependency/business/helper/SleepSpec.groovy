package co.tala.api.fakedependency.business.helper


import spock.lang.Specification

import java.time.Instant

class SleepSpec extends Specification {
    def "Sleep.forMillis should pause the thread for the specified duration"() {
        given:
            ISleep sut = new Sleep()

        when: "it is specified to sleep for 500ms"
            def start = Instant.now()
            sut.forMillis(500)
            def end = Instant.now()
            def result = end.toEpochMilli() - start.toEpochMilli()

        then: "500 milliseconds should have passed"
            result > 300 && result < 700
    }
}
