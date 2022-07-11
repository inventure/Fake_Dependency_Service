package co.tala.api.fakedependency.business.provider

import co.tala.api.fakedependency.business.composer.IRedisKeyComposer
import co.tala.api.fakedependency.business.composer.IRedisKeyWithQueryComposer
import co.tala.api.fakedependency.business.helper.IKeyHelper
import co.tala.api.fakedependency.business.helper.IRequestExtractor
import co.tala.api.fakedependency.business.mockdata.IMockDataRetriever
import co.tala.api.fakedependency.business.mockdata.MockDataRetrieval
import co.tala.api.fakedependency.business.parser.IQueryParser
import co.tala.api.fakedependency.model.DetailedRequestPayloads
import co.tala.api.fakedependency.model.MockData
import co.tala.api.fakedependency.model.ResponseSetUpMetadata
import co.tala.api.fakedependency.redis.IRedisService
import co.tala.api.fakedependency.redis.RedisKeyPrefix
import co.tala.api.fakedependency.testutil.MockDataFactory
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import spock.lang.Specification
import spock.lang.Unroll

import javax.servlet.ServletInputStream
import javax.servlet.http.HttpServletRequest

@Unroll
class FakeDependencyProviderSpec extends Specification {
    private static final REDIS_KEYS = ["redisKey1", "redisKey2"]
    private static final REDIS_KEYS_WITH_QUERY = ["redisKeyWithQuery1", "redisKeyWithQuery2"]
    private static final QUERY_MAP = ["num": "10", "animal": "dog"]
    private static final byte[] BINARY = [1, 2, 3]
    private static final Map<String, List<String>> REQUEST_HEADERS = ["X-Header-A": ["Value A"]]

    private String requestId
    private MockData mockData
    private MockDataRetrieval mockDataRetrieval
    private HttpServletRequest requestMock
    private IRedisService redisSvcMock
    private IRedisKeyComposer redisKeyComposerMock
    private IRedisKeyWithQueryComposer redisKeyWithQueryComposerMock
    private IQueryParser queryParserMock
    private IKeyHelper keyHelperMock
    private IMockDataRetriever mockDataRetrieverMock
    private IRequestExtractor requestExtractorMock
    private ObjectMapper objectMapperMock
    private IFakeDependencyProvider sut

    def setup() {
        requestId = UUID.randomUUID().toString()
        mockData = MockDataFactory.buildDefaultMockData()
        mockDataRetrieval = new MockDataRetrieval(UUID.randomUUID().toString(), mockData)
        requestMock = Mock()
        redisSvcMock = Mock()
        redisKeyComposerMock = Mock()
        redisKeyWithQueryComposerMock = Mock()
        queryParserMock = Mock()
        keyHelperMock = Mock()
        mockDataRetrieverMock = Mock()
        requestExtractorMock = Mock()
        objectMapperMock = Mock()
        sut = new FakeDependencyProvider(
            redisSvcMock,
            queryParserMock,
            redisKeyComposerMock,
            redisKeyWithQueryComposerMock,
            mockDataRetrieverMock,
            keyHelperMock,
            requestExtractorMock,
            objectMapperMock
        )
    }

    def "setup should set the mock data without query params"() {
        given: "the request has no query params"
        and: "there are 2 redisKeys"
            def query = [:]
            1 * requestExtractorMock.getRequestId(requestMock) >> requestId
            1 * redisKeyComposerMock.getKeys(requestId, requestMock, false) >> REDIS_KEYS
            1 * queryParserMock.getQuery(requestMock) >> query
            REDIS_KEYS.eachWithIndex { String redisKey, int i ->
                def redisKeyWithQuery = REDIS_KEYS_WITH_QUERY[i]
                1 * keyHelperMock.concatenateKeys(redisKey, *_) >> { args ->
                    def params = args[0] as List<String>
                    assert params.size() == 1
                    assert params[0] == redisKey
                    redisKeyWithQuery
                }
                1 * redisSvcMock.pushListValue(RedisKeyPrefix.EXECUTE, redisKeyWithQuery, mockData)
            }

        when: "setup is invoked"
            def result = sut.setup(mockData, requestMock)

        then: "redisSvc.setValue should not be invoked for the QUERY_KEY"
            0 * redisSvcMock.pushSetValue(RedisKeyPrefix.QUERY, _, _)
        and: "the mock data should be returned"
            result.body == mockData
    }

    def "setup should set the mock data with query params"() {
        given: "the request has 2 query params"
        and: "there are 2 redisKeys"
            1 * requestExtractorMock.getRequestId(requestMock) >> requestId
            1 * redisKeyComposerMock.getKeys(requestId, requestMock, false) >> REDIS_KEYS
            1 * queryParserMock.getQuery(requestMock) >> QUERY_MAP
            REDIS_KEYS.eachWithIndex { String redisKey, int i ->
                1 * redisSvcMock.pushSetValue(RedisKeyPrefix.QUERY, redisKey, QUERY_MAP)
                def redisKeyWithQuery = REDIS_KEYS_WITH_QUERY[i]
                1 * keyHelperMock.concatenateKeys(redisKey, *_) >> { args ->
                    def params = args[0] as List<String>
                    assert params == [redisKey, "num", "animal", "10", "dog"]
                    redisKeyWithQuery
                }
                1 * redisSvcMock.pushListValue(RedisKeyPrefix.EXECUTE, redisKeyWithQuery, mockData)
            }

        when: "setup is invoked"
            def result = sut.setup(mockData, requestMock)

        then: "redisSvc.setValue should be invoked for each redisKey"
        and: "the mock data should be returned"
            result.body == mockData
    }

    def "setup should set the mock data with non-standard status code"() {
        given: "the request has no query params"

        and: "ResponseSetUpMetadata has non-standard status code"
            mockData = MockDataFactory.buildCustomMockData([:], 499)

        and: "there are 2 redisKeys"
            def query = [:]
            1 * requestExtractorMock.getRequestId(requestMock) >> requestId
            1 * redisKeyComposerMock.getKeys(requestId, requestMock, false) >> REDIS_KEYS
            1 * queryParserMock.getQuery(requestMock) >> query
            REDIS_KEYS.eachWithIndex { String redisKey, int i ->
                def redisKeyWithQuery = REDIS_KEYS_WITH_QUERY[i]
                1 * keyHelperMock.concatenateKeys(redisKey, *_) >> { args ->
                    def params = args[0] as List<String>
                    assert params.size() == 1
                    assert params[0] == redisKey
                    redisKeyWithQuery
                }
                1 * redisSvcMock.pushListValue(RedisKeyPrefix.EXECUTE, redisKeyWithQuery, mockData)
            }

        when: "setup is invoked"
            def result = sut.setup(mockData, requestMock)

        then: "redisSvc.setValue should not be invoked for the QUERY_KEY"
            0 * redisSvcMock.pushSetValue(RedisKeyPrefix.QUERY, _, _)
        and: "the mock data should be returned"
            result.body == mockData
    }

    def "patch setup should store the binary in redis"() {
        given:
            1 * requestExtractorMock.getRequestId(requestMock) >> requestId
            1 * redisKeyComposerMock.getKeys(requestId, requestMock, false) >> REDIS_KEYS
            1 * queryParserMock.getQuery(requestMock) >> QUERY_MAP
            ServletInputStream streamMock = Mock()
            streamMock.readAllBytes() >> BINARY
            requestMock.getInputStream() >> streamMock
            REDIS_KEYS.eachWithIndex { String redisKey, int i ->
                def redisKeyWithQuery = REDIS_KEYS_WITH_QUERY[i]
                1 * keyHelperMock.concatenateKeys(redisKey, *_) >> { args ->
                    def params = args[0] as List<String>
                    assert params == [redisKey, "num", "animal", "10", "dog"]
                    redisKeyWithQuery
                }
                1 * redisSvcMock.pushListValue(RedisKeyPrefix.BINARY, redisKeyWithQuery, BINARY)
            }

        when: "patchSetup is invoked"
            def result = sut.patchSetup(requestMock)

        then: "redisSvc.setValue should be invoked for the query"
        and: "HTTP 204 should be returned"
            result.statusCode == HttpStatus.NO_CONTENT
    }

    def "patch setup should NOT store the binary in redis if the binary is null"() {
        given:
            byte[] bytes = null
            1 * requestExtractorMock.getRequestId(requestMock) >> requestId
            1 * redisKeyComposerMock.getKeys(requestId, requestMock, false) >> REDIS_KEYS
            1 * queryParserMock.getQuery(requestMock) >> QUERY_MAP
            ServletInputStream streamMock = Mock()
            streamMock.readAllBytes() >> bytes
            requestMock.getInputStream() >> streamMock
            REDIS_KEYS.eachWithIndex { String redisKey, int i ->
                def redisKeyWithQuery = REDIS_KEYS_WITH_QUERY[i]
                1 * keyHelperMock.concatenateKeys(redisKey, *_) >> { args ->
                    def params = args[0] as List<String>
                    assert params == [redisKey, "num", "animal", "10", "dog"]
                    redisKeyWithQuery
                }
                0 * redisSvcMock.setValue(RedisKeyPrefix.BINARY, redisKeyWithQuery, _)
            }

        when: "patchSetup is invoked"
            def result = sut.patchSetup(requestMock)

        then: "redisSvc.setValue should be invoked for each redisKey"
        and: "HTTP 204 should be returned"
            result.statusCode == HttpStatus.NO_CONTENT
    }

    def "execute should get the mock data and update request history with a binary request payload"() {
        given:
            String payloadString = new String(BINARY)
            ServletInputStream streamMock = Mock()
            streamMock.readAllBytes() >> BINARY
            requestMock.getInputStream() >> streamMock
            1 * requestExtractorMock.getRequestId(requestMock) >> requestId
            1 * requestExtractorMock.getRequestHeaders(requestMock) >> REQUEST_HEADERS
            1 * redisKeyWithQueryComposerMock.getKeys(requestId, requestMock, payloadString) >> REDIS_KEYS_WITH_QUERY
            1 * mockDataRetrieverMock.getMockData(REDIS_KEYS_WITH_QUERY, requestMock, payloadString) >> mockDataRetrieval
            1 * objectMapperMock.readValue(payloadString, _) >> {
                throw new Exception()
            }
            1 * redisSvcMock.pushListValue(RedisKeyPrefix.VERIFY_PAYLOAD, mockDataRetrieval.redisKey, payloadString)
            1 * redisSvcMock.pushListValue(RedisKeyPrefix.VERIFY_HEADERS, mockDataRetrieval.redisKey, REQUEST_HEADERS)
            1 * redisSvcMock.setValue(RedisKeyPrefix.VERIFY_PAYLOAD, mockDataRetrieval.redisKey, BINARY)

        when:
            def result = sut.execute(requestMock)

        then:
            result != null
            result.body == mockData.responseBody
            result.getHeaders() == mockData.responseHeaders
    }

    def "execute should convert the request payload to Map if it can be"() {
        given:
            def payloadString = new String(BINARY)
            def payloadMap = ["some": "map"]
            ServletInputStream streamMock = Mock()
            streamMock.readAllBytes() >> BINARY
            requestMock.getInputStream() >> streamMock
            1 * requestExtractorMock.getRequestId(requestMock) >> requestId
            1 * requestExtractorMock.getRequestHeaders(requestMock) >> REQUEST_HEADERS
            1 * redisKeyWithQueryComposerMock.getKeys(requestId, requestMock, payloadMap) >> REDIS_KEYS_WITH_QUERY
            1 * mockDataRetrieverMock.getMockData(REDIS_KEYS_WITH_QUERY, requestMock, payloadMap) >> mockDataRetrieval
            1 * objectMapperMock.readValue(payloadString, _) >> {
                payloadMap
            }
            1 * redisSvcMock.pushListValue(RedisKeyPrefix.VERIFY_PAYLOAD, mockDataRetrieval.redisKey, payloadMap)
            1 * redisSvcMock.pushListValue(RedisKeyPrefix.VERIFY_HEADERS, mockDataRetrieval.redisKey, REQUEST_HEADERS)
            1 * redisSvcMock.setValue(RedisKeyPrefix.VERIFY_PAYLOAD, mockDataRetrieval.redisKey, BINARY)

        when:
            def result = sut.execute(requestMock)

        then:
            result != null
            result.body == mockData.responseBody
            result.getHeaders() == mockData.responseHeaders
    }

    @Unroll
    def "execute should return non-standard status code #title"() {
        given:
            def payloadString = new String(BINARY)
            def payloadMap = ["some": "map"]
            ServletInputStream streamMock = Mock()
            streamMock.readAllBytes() >> BINARY
            requestMock.getInputStream() >> streamMock
            1 * requestExtractorMock.getRequestId(requestMock) >> requestId
            1 * requestExtractorMock.getRequestHeaders(requestMock) >> REQUEST_HEADERS
            1 * redisKeyWithQueryComposerMock.getKeys(requestId, requestMock, payloadMap) >> REDIS_KEYS_WITH_QUERY
            mockDataRetrieval = new MockDataRetrieval(UUID.randomUUID().toString(),
                MockDataFactory.buildCustomMockData([:], 499))
            1 * mockDataRetrieverMock.getMockData(REDIS_KEYS_WITH_QUERY, requestMock, payloadMap) >> mockDataRetrieval
            1 * objectMapperMock.readValue(payloadString, _) >> {payloadMap }
            1 * redisSvcMock.pushListValue(RedisKeyPrefix.VERIFY_PAYLOAD, mockDataRetrieval.redisKey, payloadMap)
            1 * redisSvcMock.pushListValue(RedisKeyPrefix.VERIFY_HEADERS, mockDataRetrieval.redisKey, REQUEST_HEADERS)
            1 * redisSvcMock.setValue(RedisKeyPrefix.VERIFY_PAYLOAD, mockDataRetrieval.redisKey, BINARY)

        when:
            def result = sut.execute(requestMock)

        then:
            result != null
            result.body == mockDataRetrieval.mockData.responseBody
            result.getHeaders() == mockData.responseHeaders

        where:
            title             | headers
            "with headers"    | ["X-Some-Header": ["some value"]]
            "without headers" | [:]
    }

    def "execute should update request history with empty string if the Http method is GET or DELETE"() {
        given:
            ServletInputStream streamMock = Mock()
            streamMock.readAllBytes() >> BINARY
            requestMock.getInputStream() >> streamMock
            1 * requestExtractorMock.getRequestId(requestMock) >> requestId
            1 * requestExtractorMock.getRequestHeaders(requestMock) >> REQUEST_HEADERS
            1 * requestMock.getMethod() >> method
            1 * redisKeyWithQueryComposerMock.getKeys(requestId, requestMock, null) >> REDIS_KEYS_WITH_QUERY
            1 * mockDataRetrieverMock.getMockData(REDIS_KEYS_WITH_QUERY, requestMock, null) >> mockDataRetrieval
            1 * redisSvcMock.pushListValue(RedisKeyPrefix.VERIFY_PAYLOAD, mockDataRetrieval.redisKey, "")
            1 * redisSvcMock.pushListValue(RedisKeyPrefix.VERIFY_HEADERS, mockDataRetrieval.redisKey, REQUEST_HEADERS)
            1 * redisSvcMock.setValue(RedisKeyPrefix.VERIFY_PAYLOAD, mockDataRetrieval.redisKey, BINARY)

        when:
            def result = sut.execute(requestMock)

        then:
            result != null
            result.body == mockData.responseBody
            result.getHeaders() == mockData.responseHeaders

        where:
            method << ["GET", "DELETE"]
    }


    def "verifyList should get a list of the request history"() {
        given:
            1 * requestExtractorMock.getRequestId(requestMock) >> requestId
            1 * redisKeyWithQueryComposerMock.getKeys(requestId, requestMock, null) >> REDIS_KEYS_WITH_QUERY
            def history = ["history"]
            1 * redisSvcMock.getListValues(RedisKeyPrefix.VERIFY_PAYLOAD, REDIS_KEYS_WITH_QUERY.first(), _) >> history

        when:
            def result = sut.verifyList(requestMock)

        then:
            result.body == history
    }

    def "verifyDetailed should get a detailed list of the request history"() {
        given:
            1 * requestExtractorMock.getRequestId(requestMock) >> requestId
            1 * redisKeyWithQueryComposerMock.getKeys(requestId, requestMock, null) >> REDIS_KEYS_WITH_QUERY
            def payloadHistory = ["history"]
            1 * redisSvcMock.getListValues(RedisKeyPrefix.VERIFY_PAYLOAD, REDIS_KEYS_WITH_QUERY.first(), _) >> payloadHistory
            def headerHistory = [["X-Header-A": ["a"]]]
            1 * redisSvcMock.getListValues(RedisKeyPrefix.VERIFY_HEADERS, REDIS_KEYS_WITH_QUERY.first(), _) >> headerHistory
            DetailedRequestPayloads expected = new DetailedRequestPayloads(
                1,
                [new DetailedRequestPayloads.RequestPayload(payloadHistory.first(), headerHistory.first())]
            )

        when:
            def result = sut.verifyDetailed(requestMock)

        then:
            result.body == expected
    }

    def "verifyLast should get the last in the request history as a binary"() {
        given:
            1 * requestExtractorMock.getRequestId(requestMock) >> requestId
            1 * redisKeyWithQueryComposerMock.getKeys(requestId, requestMock, null) >> REDIS_KEYS_WITH_QUERY
            def binary = "123".getBytes()
            1 * redisSvcMock.getValue(RedisKeyPrefix.VERIFY_PAYLOAD, REDIS_KEYS_WITH_QUERY.first(), _) >> binary

        when:
            def result = sut.verifyLast(requestMock)

        then:
            result.body == binary
    }

    def "verifyLast should return empty binary if the key is not found"() {
        given:
            1 * requestExtractorMock.getRequestId(requestMock) >> requestId
            1 * redisKeyWithQueryComposerMock.getKeys(requestId, requestMock, null) >> REDIS_KEYS_WITH_QUERY
            1 * redisSvcMock.getValue(RedisKeyPrefix.VERIFY_PAYLOAD, REDIS_KEYS_WITH_QUERY.first(), _) >> null

        when:
            def result = sut.verifyLast(requestMock)

        then:
            result.body == new byte[0]
    }

}
