package co.tala.api.fakedependency.business.composer

import co.tala.api.fakedependency.business.helper.IKeyHelper
import spock.lang.Specification
import spock.lang.Unroll

import javax.servlet.http.HttpServletRequest

@Unroll
class RedisKeyComposerSpec extends Specification {
    private static final URIS = [
        "/mock-resources/things/123",
        "/mock-resources/things/123",
        "/things/mock-resources/123",
        "/things/mock-resources/123",
        "/things/123/mock-resources",
        "/things/123/mock-resources",
        "/things/123",
    ]

    private IKeyHelper keyHelperMock
    private HttpServletRequest requestMock
    private IRedisKeyComposer sut
    private String requestId

    def setup() {
        keyHelperMock = Mock()
        requestMock = Mock()
        requestId = UUID.randomUUID().toString()
        sut = new RedisKeyComposer(keyHelperMock)
    }

    def "getKeys should return all keys, and should remove /mock-resources"() {
        given:
            2 * requestMock.getRequestURI() >> uri
            2 * keyHelperMock.concatenateKeys(_) >> { arg ->
                def params = arg[0] as List<String>
                assert params[0] == requestId
                assert params[1] == "/things/123"
                "key0"
            } >> { arg ->
                def params = arg[0] as List<String>
                assert params[0] == "/things/123"
                "key1"
            }

        when:
            List<String> result = sut.getKeys(requestId, requestMock, true)

        then:
            verifyAll(result) {
                it.size() == 2
                it.get(0) == "key0"
                it.get(1) == "key1"
            }

        where:
            uri << URIS

    }

    def "getKeys should return all keys with only requestId if includeWithoutRequestId is false"() {
        given:
            1 * requestMock.getRequestURI() >> uri
            1 * keyHelperMock.concatenateKeys(_) >> { arg ->
                def params = arg[0] as List<String>
                assert params[0] == requestId
                assert params[1] == "/things/123"
                "key0"
            }

        when:
            List<String> result = sut.getKeys(requestId, requestMock, false)

        then:
            verifyAll(result) {
                it.size() == 1
                it.get(0) == "key0"
            }

        where:
            uri << URIS

    }

    def "getKeys should return 1 key when the requestId is null"() {
        given:
            2 * requestMock.getRequestURI() >> uri
            2 * keyHelperMock.concatenateKeys(_) >> { arg ->
                def params = arg[0] as List<String>
                assert params[0] == "/things/123"
                "key0"
            }

        when:
            List<String> result = sut.getKeys(null, requestMock, true)

        then:
            verifyAll(result) {
                it.size() == 1
                it.get(0) == "key0"
            }

        where:
            uri << URIS
    }

}
