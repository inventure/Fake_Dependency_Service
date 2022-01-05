package co.tala.api.fakedependency.business.provider

import co.tala.api.fakedependency.business.composer.IHKeyComposer
import co.tala.api.fakedependency.business.composer.IHKeyWithQueryComposer
import co.tala.api.fakedependency.business.helper.IKeyHelper
import co.tala.api.fakedependency.business.helper.IRequestIdExtractor
import co.tala.api.fakedependency.business.mockdata.IMockDataRetriever
import co.tala.api.fakedependency.business.parser.IQueryParser
import co.tala.api.fakedependency.model.MockData
import co.tala.api.fakedependency.redis.IRedisService
import co.tala.api.fakedependency.redis.RedisKey
import co.tala.api.fakedependency.testutil.MockDataFactory
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import spock.lang.Specification
import spock.lang.Unroll

import javax.servlet.ServletInputStream
import javax.servlet.http.HttpServletRequest

@Unroll
class FakeDependencyProviderSpec extends Specification {
    private static final HKEYS = ["hkey1", "hkey2"]
    private static final HKEYS_WITH_QUERY = ["hkeyWithQuery1", "hkeyWithQuery2"]
    private static final QUERY_MAP = ["num": "10", "animal": "dog"]
    private static final byte[] BINARY = [1, 2, 3]

    private String requestId
    private MockData mockData
    private HttpServletRequest requestMock
    private IRedisService redisSvcMock
    private IHKeyComposer hKeyComposerMock
    private IHKeyWithQueryComposer hKeyWithQueryComposerMock
    private IQueryParser queryParserMock
    private IKeyHelper keyHelperMock
    private IMockDataRetriever mockDataRetrieverMock
    private IRequestIdExtractor requestIdExtractorMock
    private ObjectMapper objectMapperMock
    private FakeDependencyProvider sut

    def setup() {
        requestId = UUID.randomUUID().toString()
        mockData = MockDataFactory.buildDefaultMockData()
        requestMock = Mock()
        redisSvcMock = Mock()
        hKeyComposerMock = Mock()
        hKeyWithQueryComposerMock = Mock()
        queryParserMock = Mock()
        keyHelperMock = Mock()
        mockDataRetrieverMock = Mock()
        requestIdExtractorMock = Mock()
        objectMapperMock = Mock()
        sut = new FakeDependencyProvider(
            redisSvcMock,
            queryParserMock,
            hKeyComposerMock,
            hKeyWithQueryComposerMock,
            mockDataRetrieverMock,
            keyHelperMock,
            requestIdExtractorMock,
            objectMapperMock
        )
    }

    def "setup should set the mock data without query params"() {
        given: "the request has no query params"
        and: "there are 2 hKeys"
            def query = [:]
            1 * requestIdExtractorMock.getRequestId(requestMock) >> requestId
            1 * hKeyComposerMock.getKeys(requestId, requestMock) >> HKEYS
            1 * queryParserMock.getQuery(requestMock) >> query
            HKEYS.eachWithIndex { String hKey, int i ->
                def hKeyWithQuery = HKEYS_WITH_QUERY[i]
                1 * keyHelperMock.concatenateKeys(hKey, *_) >> { args ->
                    def params = args[0] as List<String>
                    assert params.size() == 1
                    assert params[0] == hKey
                    hKeyWithQuery
                }
                1 * redisSvcMock.setValue(RedisKey.EXECUTE_KEY, hKeyWithQuery, mockData)
            }

        when: "setup is invoked"
            def result = sut.setup(mockData, requestMock)

        then: "redisSvc.setValue should not be invoked for the QUERY_KEY"
            0 * redisSvcMock.setValue(RedisKey.QUERY_KEY, _, _)
        and: "the mock data should be returned"
            result.body == mockData
    }

    def "setup should set the mock data with query params"() {
        given: "the request has 2 query params"
        and: "there are 2 hKeys"
            1 * requestIdExtractorMock.getRequestId(requestMock) >> requestId
            1 * hKeyComposerMock.getKeys(requestId, requestMock) >> HKEYS
            1 * queryParserMock.getQuery(requestMock) >> QUERY_MAP
            HKEYS.eachWithIndex { String hKey, int i ->
                1 * redisSvcMock.setValue(RedisKey.QUERY_KEY, hKey, QUERY_MAP)
                def hKeyWithQuery = HKEYS_WITH_QUERY[i]
                1 * keyHelperMock.concatenateKeys(hKey, *_) >> { args ->
                    def params = args[0] as List<String>
                    assert params[0] == hKey
                    assert params[1] == QUERY_MAP.values()[0]
                    assert params[2] == QUERY_MAP.values()[1]
                    hKeyWithQuery
                }
                1 * redisSvcMock.setValue(RedisKey.EXECUTE_KEY, hKeyWithQuery, mockData)
            }

        when: "setup is invoked"
            def result = sut.setup(mockData, requestMock)

        then: "redisSvc.setValue should be invoked for each hKey"
        and: "the mock data should be returned"
            result.body == mockData
    }

    def "patch setup should store the binary in redis"() {
        given:
            1 * requestIdExtractorMock.getRequestId(requestMock) >> requestId
            1 * hKeyComposerMock.getKeys(requestId, requestMock) >> HKEYS
            1 * queryParserMock.getQuery(requestMock) >> QUERY_MAP
            ServletInputStream streamMock = Mock()
            streamMock.readAllBytes() >> BINARY
            requestMock.getInputStream() >> streamMock
            HKEYS.eachWithIndex { String hKey, int i ->
                def hKeyWithQuery = HKEYS_WITH_QUERY[i]
                1 * keyHelperMock.concatenateKeys(hKey, *_) >> { args ->
                    def params = args[0] as List<String>
                    assert params[0] == hKey
                    assert params[1] == QUERY_MAP.values()[0]
                    assert params[2] == QUERY_MAP.values()[1]
                    hKeyWithQuery
                }
                1 * redisSvcMock.setValue(RedisKey.BINARY_KEY, hKeyWithQuery, BINARY)
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
            1 * requestIdExtractorMock.getRequestId(requestMock) >> requestId
            1 * hKeyComposerMock.getKeys(requestId, requestMock) >> HKEYS
            1 * queryParserMock.getQuery(requestMock) >> QUERY_MAP
            ServletInputStream streamMock = Mock()
            streamMock.readAllBytes() >> bytes
            requestMock.getInputStream() >> streamMock
            HKEYS.eachWithIndex { String hKey, int i ->
                def hKeyWithQuery = HKEYS_WITH_QUERY[i]
                1 * keyHelperMock.concatenateKeys(hKey, *_) >> { args ->
                    def params = args[0] as List<String>
                    assert params[0] == hKey
                    assert params[1] == QUERY_MAP.values()[0]
                    assert params[2] == QUERY_MAP.values()[1]
                    hKeyWithQuery
                }
                0 * redisSvcMock.setValue(RedisKey.BINARY_KEY, hKeyWithQuery, _)
            }

        when: "patchSetup is invoked"
            def result = sut.patchSetup(requestMock)

        then: "redisSvc.setValue should be invoked for each hKey"
        and: "HTTP 204 should be returned"
            result.statusCode == HttpStatus.NO_CONTENT
    }

    def "execute should get the mock data and update request history with a binary request payload"() {
        given:
            String payloadString = new String(BINARY)
            ServletInputStream streamMock = Mock()
            streamMock.readAllBytes() >> BINARY
            requestMock.getInputStream() >> streamMock
            1 * requestIdExtractorMock.getRequestId(requestMock) >> requestId
            1 * mockDataRetrieverMock.getMockData(requestId, requestMock, payloadString) >> mockData
            1 * hKeyWithQueryComposerMock.getKeys(requestId, requestMock, payloadString) >> HKEYS_WITH_QUERY
            1 * objectMapperMock.readValue(payloadString, _) >> {
                throw new Exception()
            }
            HKEYS_WITH_QUERY.each {
                1 * redisSvcMock.pushListValue(RedisKey.VERIFY_KEY, it, payloadString)
            }

        when:
            def result = sut.execute(requestMock)

        then:
            result != null
            result.body == mockData.responseBody
    }

    def "execute should convert the request payload to Map if it can be"() {
        given:
            def payloadString = new String(BINARY)
            def payloadMap = ["some": "map"]
            ServletInputStream streamMock = Mock()
            streamMock.readAllBytes() >> BINARY
            requestMock.getInputStream() >> streamMock
            1 * requestIdExtractorMock.getRequestId(requestMock) >> requestId
            1 * mockDataRetrieverMock.getMockData(requestId, requestMock, payloadMap) >> mockData
            1 * hKeyWithQueryComposerMock.getKeys(requestId, requestMock, payloadMap) >> HKEYS_WITH_QUERY
            1 * objectMapperMock.readValue(payloadString, _) >> {
                payloadMap
            }
            HKEYS_WITH_QUERY.each {
                1 * redisSvcMock.pushListValue(RedisKey.VERIFY_KEY, it, payloadMap)
            }

        when:
            def result = sut.execute(requestMock)

        then:
            result != null
            result.body == mockData.responseBody
    }

    def "execute should update request history with empty string if the Http method is GET or DELETE"() {
        given:
            1 * requestIdExtractorMock.getRequestId(requestMock) >> requestId
            1 * requestMock.getMethod() >> method
            1 * mockDataRetrieverMock.getMockData(requestId, requestMock, null) >> mockData
            1 * hKeyWithQueryComposerMock.getKeys(requestId, requestMock, null) >> HKEYS_WITH_QUERY
            HKEYS_WITH_QUERY.each {
                1 * redisSvcMock.pushListValue(RedisKey.VERIFY_KEY, it, "")
            }

        when:
            def result = sut.execute(requestMock)

        then:
            result != null
            result.body == mockData.responseBody

        where:
            method << ["GET", "DELETE"]
    }


    def "verify should get the request history"() {
        given:
            1 * requestIdExtractorMock.getRequestId(requestMock) >> requestId
            1 * queryParserMock.getQuery(requestMock) >> QUERY_MAP
            1 * hKeyWithQueryComposerMock.getKeys(requestId, requestMock, QUERY_MAP) >> HKEYS_WITH_QUERY
            def history = ["history"]
            1 * redisSvcMock.getListValues(RedisKey.VERIFY_KEY, HKEYS_WITH_QUERY.first(), _) >> history

        when:
            def result = sut.verify(requestMock)

        then:
            result.body == history
    }

}
