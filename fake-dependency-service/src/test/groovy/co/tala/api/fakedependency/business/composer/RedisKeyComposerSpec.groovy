package co.tala.api.fakedependency.business.composer

import co.tala.api.fakedependency.business.helper.IKeyHelper
import co.tala.api.fakedependency.business.helper.IRequestExtractor
import co.tala.api.fakedependency.constant.HttpMethod
import co.tala.api.fakedependency.redis.IRedisService
import co.tala.api.fakedependency.redis.RedisKeyPrefix
import com.fasterxml.jackson.core.type.TypeReference
import spock.lang.Specification
import spock.lang.Unroll

import javax.servlet.http.HttpServletRequest

@Unroll
class RedisKeyComposerSpec extends Specification {
    private static final URIS_WITH_MOCK_RESOURCES = [
        "/mock-resources/things/123",
        "/mock-resources/things/123",
        "/mock-resources/things/123",
        "/things/mock-resources/123",
        "/things/mock-resources/123",
        "/things/mock-resources/123",
        "/things/123/mock-resources",
        "/things/123/mock-resources",
        "/things/123/mock-resources"
    ]
    private static final URI_WITHOUT_MOCK_RESOURCES = "/things/123"

    private IKeyHelper keyHelperMock
    private HttpServletRequest requestMock
    private IRequestExtractor requestExtractorMock
    private IRedisService redisServiceMock
    private IRedisKeyComposer sut
    private String requestId

    def setup() {
        keyHelperMock = Mock()
        requestMock = Mock()
        requestExtractorMock = Mock()
        redisServiceMock = Mock()
        requestId = UUID.randomUUID().toString()
        sut = new RedisKeyComposer(keyHelperMock, requestExtractorMock, redisServiceMock)
    }

    def "When request is for setting up mock with http method, getKeys should return all keys, should store the http method in redis, and should remove /mock-resources: uri=#uri, httpMethod=#httpMethod"() {
        given:
            2 * requestMock.getRequestURI() >> uri
            1 * requestMock.getMethod() >> HttpMethod.POST.toString()
            1 * requestExtractorMock.getHttpMethod(requestMock) >> httpMethod
            2 * keyHelperMock.concatenateKeys(_) >> { arg ->
                def params = arg[0] as List<String>
                assert params[0] == requestId
                assert params[1] == "/${httpMethod.value}"
                assert params[2] == "/things/123"
                "keyWithHttpMethod"
            } >> { arg ->
                def params = arg[0] as List<String>
                assert params[0] == requestId
                assert params[1] == "/things/123"
                "keyWithoutHttpMethod"
            }

        when:
            List<String> result = sut.getKeys(requestId, requestMock, false)

        then:
            1 * redisServiceMock.pushSetValues(RedisKeyPrefix.HTTP_METHOD, "keyWithoutHttpMethod", [httpMethod])
            verifyAll(result) {
                it.size() == 1
                it.get(0) == "keyWithHttpMethod"
            }

        where:
            uri << URIS_WITH_MOCK_RESOURCES
            httpMethod << HttpMethod.values().findAll { it != HttpMethod.NONE }

    }

    def "When request is for setting up mock with http method, getKeys should return all keys, but should store the http method in redis when method is not POST, and should remove /mock-resources: uri=#uri, httpMethod=#httpMethod"() {
        given:
            2 * requestMock.getRequestURI() >> uri
            1 * requestMock.getMethod() >> HttpMethod.GET.toString()
            1 * requestExtractorMock.getHttpMethod(requestMock) >> httpMethod
            2 * keyHelperMock.concatenateKeys(_) >> { arg ->
                def params = arg[0] as List<String>
                assert params[0] == requestId
                assert params[1] == "/${httpMethod.value}"
                assert params[2] == "/things/123"
                "keyWithHttpMethod"
            } >> { arg ->
                def params = arg[0] as List<String>
                assert params[0] == requestId
                assert params[1] == "/things/123"
                "keyWithoutHttpMethod"
            }

        when:
            List<String> result = sut.getKeys(requestId, requestMock, false)

        then:
            0 * redisServiceMock.pushSetValues(_, _, _)
            verifyAll(result) {
                it.size() == 1
                it.get(0) == "keyWithHttpMethod"
            }

        where:
            uri << URIS_WITH_MOCK_RESOURCES
            httpMethod << HttpMethod.values().findAll { it != HttpMethod.NONE }

    }

    def "When request is for setting up mock without http method, getKeys should return all keys, should NOT store the http method in redis, and should remove /mock-resources: uri=#uri, httpMethod=#httpMethod"() {
        given:
            2 * requestMock.getRequestURI() >> uri
            1 * requestMock.getMethod() >> HttpMethod.POST.toString()
            1 * requestExtractorMock.getHttpMethod(requestMock) >> httpMethod
            2 * keyHelperMock.concatenateKeys(_) >> { arg ->
                def params = arg[0] as List<String>
                assert params[0] == requestId
                // HttpMethod is None, therefore null
                assert params[1] == "/things/123"
                "keyWithHttpMethod"
            } >> { arg ->
                def params = arg[0] as List<String>
                assert params[0] == requestId
                assert params[1] == "/things/123"
                "keyWithoutHttpMethod"
            }

        when:
            List<String> result = sut.getKeys(requestId, requestMock, false)

        then:
            0 * redisServiceMock.pushSetValues(_, _, _)
            verifyAll(result) {
                it.size() == 1
                it.get(0) == "keyWithHttpMethod"
            }

        where:
            uri << URIS_WITH_MOCK_RESOURCES
            httpMethod = HttpMethod.NONE

    }

    def "When request is for executing a mock and http method exists in Redis, getKeys should return the key with the http method: uri=#uri, httpMethod=#httpMethod"() {
        given:
            2 * requestMock.getRequestURI() >> uri
            0 * requestMock.getMethod()
            1 * requestExtractorMock.getHttpMethod(requestMock) >> httpMethod
            1 * redisServiceMock.getSetValues(
                RedisKeyPrefix.HTTP_METHOD,
                "keyWithoutHttpMethod",
                _ as TypeReference
            ) >> [httpMethod]
            2 * keyHelperMock.concatenateKeys(_) >> { arg ->
                def params = arg[0] as List<String>
                assert params[0] == requestId
                assert params[1] == "/${httpMethod.value}"
                assert params[2] == "/things/123"
                "keyWithHttpMethod"
            } >> { arg ->
                def params = arg[0] as List<String>
                assert params[0] == requestId
                assert params[1] == "/things/123"
                "keyWithoutHttpMethod"
            }

        when:
            List<String> result = sut.getKeys(requestId, requestMock, false)

        then:
            verifyAll(result) {
                it.size() == 1
                it.get(0) == "keyWithHttpMethod"
            }

        where:
            httpMethod << HttpMethod.values().findAll { it != HttpMethod.NONE }
            uri = URI_WITHOUT_MOCK_RESOURCES

    }

    def "When request is for executing a mock and http method does NOT exist in Redis, getKeys should return the key without the http method: uri=#uri, httpMethod=#httpMethod"() {
        given:
            2 * requestMock.getRequestURI() >> uri
            0 * requestMock.getMethod()
            1 * requestExtractorMock.getHttpMethod(requestMock) >> httpMethod
            1 * redisServiceMock.getSetValues(
                RedisKeyPrefix.HTTP_METHOD,
                "keyWithoutHttpMethod",
                _ as TypeReference
            ) >> []
            2 * keyHelperMock.concatenateKeys(_) >> { arg ->
                def params = arg[0] as List<String>
                assert params[0] == requestId
                assert params[1] == "/${httpMethod.value}"
                assert params[2] == "/things/123"
                "keyWithHttpMethod"
            } >> { arg ->
                def params = arg[0] as List<String>
                assert params[0] == requestId
                assert params[1] == "/things/123"
                "keyWithoutHttpMethod"
            }

        when:
            List<String> result = sut.getKeys(requestId, requestMock, false)

        then:
            verifyAll(result) {
                it.size() == 1
                it.get(0) == "keyWithoutHttpMethod"
            }

        where:
            httpMethod << HttpMethod.values().findAll { it != HttpMethod.NONE }
            uri = URI_WITHOUT_MOCK_RESOURCES

    }

    def "getKeys should return keys with and without requestId when includeWithoutRequestId is true: uri=#uri, httpMethod=#httpMethod"() {
        given:
            4 * requestMock.getRequestURI() >> uri
            2 * requestMock.getMethod() >> HttpMethod.POST.toString()
            1 * requestExtractorMock.getHttpMethod(requestMock) >> httpMethod
            4 * keyHelperMock.concatenateKeys(_) >> { arg ->
                def params = arg[0] as List<String>
                assert params[0] == requestId
                assert params[1] == "/${httpMethod.value}"
                assert params[2] == "/things/123"
                "keyWithHttpMethod"
            } >> { arg ->
                def params = arg[0] as List<String>
                assert params[0] == requestId
                assert params[1] == "/things/123"
                "keyWithoutHttpMethod"
            } >> { arg ->
                def params = arg[0] as List<String>
                assert params[0] == "/${httpMethod.value}"
                assert params[1] == "/things/123"
                "keyWithHttpMethodNoRequestId"
            } >> { arg ->
                def params = arg[0] as List<String>
                assert params[0] == "/things/123"
                "keyWithoutHttpMethodNoRequestId"
            }

        when:
            List<String> result = sut.getKeys(requestId, requestMock, true)

        then:
            1 * redisServiceMock.pushSetValues(RedisKeyPrefix.HTTP_METHOD, "keyWithoutHttpMethod", [httpMethod])
            1 * redisServiceMock.pushSetValues(RedisKeyPrefix.HTTP_METHOD, "keyWithoutHttpMethodNoRequestId", [httpMethod])
            verifyAll(result) {
                it.size() == 2
                it.get(0) == "keyWithHttpMethod"
                it.get(1) == "keyWithHttpMethodNoRequestId"
            }

        where:
            uri << URIS_WITH_MOCK_RESOURCES
            httpMethod << HttpMethod.values().findAll { it != HttpMethod.NONE }

    }

    def "getKeys should return distinct keys when requestId is null and includeWithoutRequestId is true: uri=#uri, httpMethod=#httpMethod"() {
        given:
            4 * requestMock.getRequestURI() >> uri
            2 * requestMock.getMethod() >> HttpMethod.POST.toString()
            1 * requestExtractorMock.getHttpMethod(requestMock) >> httpMethod
            4 * keyHelperMock.concatenateKeys(_) >> { arg ->
                def params = arg[0] as List<String>
                assert params[0] == "/${httpMethod.value}"
                assert params[1] == "/things/123"
                "keyWithHttpMethod"
            } >> { arg ->
                def params = arg[0] as List<String>
                assert params[0] == "/things/123"
                "keyWithoutHttpMethod"
            } >> { arg ->
                def params = arg[0] as List<String>
                assert params[0] == "/${httpMethod.value}"
                assert params[1] == "/things/123"
                "keyWithHttpMethod"
            } >> { arg ->
                def params = arg[0] as List<String>
                assert params[0] == "/things/123"
                "keyWithoutHttpMethod"
            }

        when:
            List<String> result = sut.getKeys(null, requestMock, true)

        then:
            2 * redisServiceMock.pushSetValues(RedisKeyPrefix.HTTP_METHOD, "keyWithoutHttpMethod", [httpMethod])
            verifyAll(result) {
                it.size() == 1
                it.get(0) == "keyWithHttpMethod"
            }

        where:
            uri << URIS_WITH_MOCK_RESOURCES
            httpMethod << HttpMethod.values().findAll { it != HttpMethod.NONE }

    }
}
