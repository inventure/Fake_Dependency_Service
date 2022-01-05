package co.tala.api.fakedependency.business.helper

import co.tala.api.fakedependency.configuration.helper.RequestIdExtractorConfiguration
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest

class RequestIdExtractorSpec extends Specification {
    def "getRequestId should return the correct header value"() {
        given: "the HttpServletRequest is mocked to return a header value"
            def config = new RequestIdExtractorConfiguration("some header key")
            HttpServletRequest requestMock = Mock()
            requestMock.getHeader(config.header) >> "some header value"
            IRequestIdExtractor sut = new RequestIdExtractor(config)

        when: "getRequestId is invoked"
            def result = sut.getRequestId(requestMock)

        then: "the header value should be returned"
            result == "some header value"
    }
}
