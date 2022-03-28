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

    def "getQuery should return a multi value map of query params"() {
        given:
            requestMock.getQueryString() >> "num=5&animal=dog&animal=cat"

        when:
            Map<String, List<String>> result = sut.getQuery(requestMock)

        then:
            result == ["num": ["5"], "animal": ["dog", "cat"]]
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
