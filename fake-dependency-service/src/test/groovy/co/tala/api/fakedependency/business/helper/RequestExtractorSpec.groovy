package co.tala.api.fakedependency.business.helper

import co.tala.api.fakedependency.configuration.helper.RequestIdExtractorConfiguration
import spock.lang.Specification
import spock.lang.Unroll

import javax.servlet.http.HttpServletRequest

@Unroll
class RequestExtractorSpec extends Specification {
    def "getRequestId should return the correct header value"() {
        given: "the HttpServletRequest is mocked to return some header values"
            HttpServletRequest requestMock = Mock()
            headers.each {
                requestMock.getHeader(it.key) >> it.value
            }
            IKeyHelper keyHelperMock = Mock()
            boolean hasHeaders = headers.isEmpty() || configHeaders.isEmpty()
            if (hasHeaders) {
                0 * keyHelperMock.concatenateKeys(_)
            } else {
                1 * keyHelperMock.concatenateKeys(expected) >> "id"
            }
            def config = new RequestIdExtractorConfiguration(configHeaders)
            IRequestExtractor sut = new RequestExtractor(keyHelperMock, config)

        when: "getRequestId is invoked"
            def result = sut.getRequestId(requestMock)

        then: "a concatenation of the header values should be returned"
            if (hasHeaders) {
                assert result == null
            } else {
                assert result == "id"
            }

        where:
            headers                   | configHeaders           | expected
            [:]                       | ""                      | null
            [:]                       | "X-Header-A"            | null
            [:]                       | "X-Header-A,X-Header-B" | null
            ["X-Header-A": "value a"] | ""                      | null
            ["X-Header-A": "value a"] | "X-Header-A"            | ["value a"]
            ["X-Header-A": "value a",
             "X-Header-B": "value b"] | "X-Header-A,X-Header-B" | ["value a", "value b"]
            ["X-Header-A": "value a",
             "X-Header-B": null]      | "X-Header-A,X-Header-B" | ["value a"]
            ["X-Header-A": "value a"] | "X-Header-A,X-Header-B" | ["value a"]
            ["X-Header-A": "value a",
             "X-Header-B": "value b"] | "X-Header-A"            | ["value a"]

    }

    def "getRequestHeaders should return a map of the headers"() {
        given: "the HttpServletRequest is mocked to return some header values"
            HttpServletRequest requestMock = Mock()
            IKeyHelper keyHelperMock = Mock()
            def config = new RequestIdExtractorConfiguration("")
            def headers = ["X-Header-A": ["A"], "X-Header-B": ["B"], "X-Header-C": ["C"]]
            def keys = Collections.enumeration(headers.collect { it.key }.asCollection())
            1 * requestMock.getHeaderNames() >> keys
            headers.each {
                1 * requestMock.getHeaders(it.key) >> Collections.enumeration(it.value.asCollection())
            }
            IRequestExtractor sut = new RequestExtractor(keyHelperMock, config)

        when: "getRequestHeaders is invoked"
            def result = sut.getRequestHeaders(requestMock)

        then: "the correct headers should be returned"
            result == headers

    }
}
