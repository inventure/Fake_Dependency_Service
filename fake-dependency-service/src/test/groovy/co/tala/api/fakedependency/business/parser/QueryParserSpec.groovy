package co.tala.api.fakedependency.business.parser


import spock.lang.Specification

import javax.servlet.http.HttpServletRequest

class QueryParserSpec extends Specification {
    private HttpServletRequest requestMock
    private IQueryParser sut

    def setup() {
        requestMock = Mock()
        sut = new QueryParser()
    }

    def "getQuery should return a map of query params"() {
        given:
            // NOTE: Spring doesn't include the '?' in the value for queryString
            requestMock.getQueryString() >> "num=5&animal=dog"

        when:
            def result = sut.getQuery(requestMock)

        then:
            verifyAll(result) {
                it["num"] == "5"
                it["animal"] == "dog"
            }

    }

    def "getQuery should return empty map if query string is null"() {
        given:
            requestMock.getQueryString() >> null

        when:
            def result = sut.getQuery(requestMock)

        then:
            result.isEmpty()
    }

}
