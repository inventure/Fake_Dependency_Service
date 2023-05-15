package co.tala.api.fakedependency.business.helper

import co.tala.api.fakedependency.configuration.helper.RequestIdExtractorConfiguration
import co.tala.api.fakedependency.constant.HttpMethod
import co.tala.api.fakedependency.exception.IllegalHttpMethodException
import co.tala.api.fakedependency.redis.IRedisService
import co.tala.api.fakedependency.redis.RedisKeyPrefix
import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Specification
import spock.lang.Unroll

import javax.servlet.http.HttpServletRequest

@Unroll
class RequestExtractorSpec extends Specification {
    private final static String X_FAKE_DEPENDENCY_PARSE_PAYLOAD_HEADER_KEY = "X-Fake-Dependency-Parse-Payload-Header"
    private final static String X_FAKE_DEPENDENCY_PARSE_PAYLOAD_HEADER_VALUE = "X-Some-Header"
    private final static String X_FAKE_DEPENDENCY_HTTP_METHOD_HEADER_KEY = "X-Fake-Dependency-Http-Method"

    private IKeyHelper keyHelperMock
    private IRedisService redisSvcMock
    private HttpServletRequest requestMock
    private ObjectMapper objectMapper

    def setup() {
        keyHelperMock = Mock()
        redisSvcMock = Mock()
        requestMock = Mock()
        objectMapper = new ObjectMapper()
    }

    def "getRequestId should return the correct header value"() {
        given: "the HttpServletRequest is mocked to return some header values"
            headers.each {
                requestMock.getHeader(it.key) >> it.value
            }
            boolean hasHeaders = headers.isEmpty() || configHeaders.isEmpty()
            if (hasHeaders) {
                0 * keyHelperMock.concatenateKeys(_)
            } else {
                1 * keyHelperMock.concatenateKeys(expected) >> "id"
            }
            def config = new RequestIdExtractorConfiguration(configHeaders)
            IRequestExtractor sut = new RequestExtractor(keyHelperMock, config, redisSvcMock, objectMapper)

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
            def config = new RequestIdExtractorConfiguration("")
            def headers = ["X-Header-A": ["A"], "X-Header-B": ["B"], "X-Header-C": ["C"]]
            def keys = Collections.enumeration(headers.collect { it.key }.asCollection())
            1 * requestMock.getHeaderNames() >> keys
            headers.each {
                1 * requestMock.getHeaders(it.key) >> Collections.enumeration(it.value.asCollection())
            }
            IRequestExtractor sut = new RequestExtractor(keyHelperMock, config, redisSvcMock, objectMapper)

        when: "getRequestHeaders is invoked"
            def result = sut.getRequestHeaders(requestMock)

        then: "the correct headers should be returned"
            result == headers
    }

    def "setPayloadRequestHeaderName should set the request header name in Redis if header X-Fake-Dependency-Parse-Payload-Header exists "() {
        given: "header X-Fake-Dependency-Parse-Payload-Header is set to return X-Some-Header"
            String redisKey = "some redis key"
            def config = new RequestIdExtractorConfiguration("")
            1 * requestMock.getHeader(X_FAKE_DEPENDENCY_PARSE_PAYLOAD_HEADER_KEY) >> X_FAKE_DEPENDENCY_PARSE_PAYLOAD_HEADER_VALUE
            IRequestExtractor sut = new RequestExtractor(keyHelperMock, config, redisSvcMock, objectMapper)

        when: "setPayloadRequestHeaderName is invoked"
            sut.setPayloadRequestHeaderName(redisKey, requestMock)

        then: "the header value should be set in Redis"
            1 * redisSvcMock.setValue(RedisKeyPrefix.PARSE_PAYLOAD_REQUEST_HEADER, redisKey, X_FAKE_DEPENDENCY_PARSE_PAYLOAD_HEADER_VALUE)

    }

    def "setPayloadRequestHeaderName should NOT set the request header name in Redis if header X-Fake-Dependency-Parse-Payload-Header does not exist "() {
        given: "header X-Fake-Dependency-Parse-Payload-Header is not set"
            String redisKey = "some redis key"
            def config = new RequestIdExtractorConfiguration("")
            1 * requestMock.getHeader(X_FAKE_DEPENDENCY_PARSE_PAYLOAD_HEADER_KEY) >> null
            IRequestExtractor sut = new RequestExtractor(keyHelperMock, config, redisSvcMock, objectMapper)

        when: "setPayloadRequestHeaderName is invoked"
            sut.setPayloadRequestHeaderName(redisKey, requestMock)

        then: "the header value should not be set in Redis"
            0 * redisSvcMock.setValue(RedisKeyPrefix.PARSE_PAYLOAD_REQUEST_HEADER, redisKey, _)

    }

    def "getPayloadFromRequestHeaders should return a map if the request header is json"() {
        given: "the request header is a valid json"
            def headerKey = "headerKey"
            def headerValue = """{"animal":"dog","number":4}"""
            def redisKey = "redisKey"
            def config = new RequestIdExtractorConfiguration("")
            1 * redisSvcMock.getValue(RedisKeyPrefix.PARSE_PAYLOAD_REQUEST_HEADER, redisKey, _) >> headerKey
            1 * requestMock.getHeader(headerKey) >> headerValue
            IRequestExtractor sut = new RequestExtractor(keyHelperMock, config, redisSvcMock, objectMapper)

        when: "getPayloadFromRequestHeaders is invoked"
            def result = sut.getPayloadFromRequestHeaders(redisKey, requestMock)

        then: "the result should be converted to a map"
            result as Map<String, Object> == ["animal": "dog", "number": 4]

    }

    def "getPayloadFromRequestHeaders should return the actual value of the header if it is not json"() {
        given: "the request header is not a valid json"
            def headerKey = "headerKey"
            def headerValue = "I'm just a string"
            def redisKey = "redisKey"
            def config = new RequestIdExtractorConfiguration("")
            1 * redisSvcMock.getValue(RedisKeyPrefix.PARSE_PAYLOAD_REQUEST_HEADER, redisKey, _) >> headerKey
            1 * requestMock.getHeader(headerKey) >> headerValue
            IRequestExtractor sut = new RequestExtractor(keyHelperMock, config, redisSvcMock, objectMapper)

        when: "getPayloadFromRequestHeaders is invoked"
            def result = sut.getPayloadFromRequestHeaders(redisKey, requestMock)

        then: "the result should be the actual header value"
            result == headerValue
    }

    def "getPayloadFromRequestHeaders should return null if the key does not exist in Redis"() {
        given: "the key in redis does not exist"
            def headerValue = "I'm just a string"
            def redisKey = "redisKey"
            def config = new RequestIdExtractorConfiguration("")
            1 * redisSvcMock.getValue(RedisKeyPrefix.PARSE_PAYLOAD_REQUEST_HEADER, redisKey, _) >> null
            0 * requestMock.getHeader(_) >> headerValue
            IRequestExtractor sut = new RequestExtractor(keyHelperMock, config, redisSvcMock, objectMapper)

        when: "getPayloadFromRequestHeaders is invoked"
            def result = sut.getPayloadFromRequestHeaders(redisKey, requestMock)

        then: "the result should be null"
            result == null
    }

    def "getHttpMethod should return value from X-Fake-Dependency-Http-Method header when uri is for setup/verify: uri=#uri, httpMethod=#httpMethod"() {
        given:
            def config = new RequestIdExtractorConfiguration("")
            1 * requestMock.getHeader(X_FAKE_DEPENDENCY_HTTP_METHOD_HEADER_KEY) >> httpMethod.toString()
            1 * requestMock.getRequestURI() >> uri
            0 * requestMock.getMethod()
            IRequestExtractor sut = new RequestExtractor(keyHelperMock, config, redisSvcMock, objectMapper)

        when:
            def result = sut.getHttpMethod(requestMock)

        then:
            result == httpMethod

        where:
            uri = "/mock-resources/thing/123"
            httpMethod << HttpMethod.values().findAll { it != HttpMethod.NONE }
    }

    def "getHttpMethod should return NONE when X-Fake-Dependency-Http-Method header does not exist and when uri is for setup/verify: uri=#uri, httpMethod=#httpMethod"() {
        def config = new RequestIdExtractorConfiguration("")
        1 * requestMock.getHeader(X_FAKE_DEPENDENCY_HTTP_METHOD_HEADER_KEY) >> httpMethod
        1 * requestMock.getRequestURI() >> uri
        0 * requestMock.getMethod()
        IRequestExtractor sut = new RequestExtractor(keyHelperMock, config, redisSvcMock, objectMapper)

        when:
            def result = sut.getHttpMethod(requestMock)

        then:
            result == HttpMethod.NONE

        where:
            uri = "/mock-resources/thing/123"
            httpMethod = null

    }

    def "getHttpMethod should throw IllegalHttpMethodException when X-Fake-Dependency-Http-Method header is invalid and when uri is for setup/verify: uri=#uri, httpMethod=#httpMethod"() {
        given:
            def config = new RequestIdExtractorConfiguration("")
            1 * requestMock.getHeader(X_FAKE_DEPENDENCY_HTTP_METHOD_HEADER_KEY) >> httpMethod
            1 * requestMock.getRequestURI() >> uri
            0 * requestMock.getMethod()
            IRequestExtractor sut = new RequestExtractor(keyHelperMock, config, redisSvcMock, objectMapper)

        when:
            sut.getHttpMethod(requestMock)

        then:
            def ex = thrown IllegalHttpMethodException
            ex.message == "INVALID is not a valid Http Method! Valid Http Methods are POST,PUT,PATCH,GET,DELETE,OPTIONS,HEAD,TRACE,CONNECT."

        where:
            uri = "/mock-resources/thing/123"
            httpMethod = "INVALID"

    }

    def "getHttpMethod should return value from actual http method when uri is for execution of mock: uri=#uri, httpMethod=#httpMethod"() {
        given:
            def config = new RequestIdExtractorConfiguration("")
            0 * requestMock.getHeader(_)
            1 * requestMock.getRequestURI() >> uri
            1 * requestMock.getMethod() >> httpMethod.toString()
            IRequestExtractor sut = new RequestExtractor(keyHelperMock, config, redisSvcMock, objectMapper)

        when:
            def result = sut.getHttpMethod(requestMock)

        then:
            result == httpMethod

        where:
            uri = "/thing/123"
            httpMethod << HttpMethod.values().findAll { it != HttpMethod.NONE }
    }
}
