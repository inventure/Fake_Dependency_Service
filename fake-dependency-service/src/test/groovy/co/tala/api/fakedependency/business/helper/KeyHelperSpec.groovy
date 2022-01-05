package co.tala.api.fakedependency.business.helper


import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class KeyHelperSpec extends Specification {
    def "concatenateKeys with many args should succeed"() {
        given:
            IKeyHelper sut = new KeyHelper()

        when:
            String result = sut.concatenateKeys(1, 2L, 3.3F, "hello")

        then:
            result == "1-2-3.3-hello"
    }

    def "concatenateKeys with no args should succeed"() {
        given:
            IKeyHelper sut = new KeyHelper()

        when:
            String result = sut.concatenateKeys()

        then:
            result == ""
    }

    def "concatenateKeys with 1 arg should succeed"() {
        given:
            IKeyHelper sut = new KeyHelper()

        when:
            String result = sut.concatenateKeys("hi")

        then:
            result == "hi"
    }

}
