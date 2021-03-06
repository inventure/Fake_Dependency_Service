package co.tala.api.fakedependency.business.mockdata

import co.tala.api.fakedependency.business.helper.ISleep
import co.tala.api.fakedependency.business.mockdata.defaultmock.IDefaultMockDataRetriever
import co.tala.api.fakedependency.model.MockData
import co.tala.api.fakedependency.redis.IRedisService
import co.tala.api.fakedependency.redis.RedisKeyPrefix
import co.tala.api.fakedependency.testutil.MockDataFactory
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest

class MockDataRetrieverSpec extends Specification {
    private static final List<String> KEYS = ["key0", "key1"]
    private static final byte[] BINARY = [1, 2, 3]
    private static final String PAYLOAD = "some payload"
    private static final MockData MOCK_DATA = MockDataFactory.buildCustomMockData("some mocked data")
    private static final MockData DEFAULT_MOCK_DATA = MockDataFactory.buildCustomMockData("some default data")

    private String requestId
    private HttpServletRequest requestMock
    private IRedisService redisSvcMock
    private IDefaultMockDataRetriever defaultMockDataRetrieverMock
    private ISleep sleepMock
    private IMockDataRetriever sut

    def setup() {
        requestId = UUID.randomUUID().toString()
        requestMock = Mock()
        redisSvcMock = Mock()
        defaultMockDataRetrieverMock = Mock()
        sleepMock = Mock()
        sut = new MockDataRetriever(redisSvcMock, defaultMockDataRetrieverMock, sleepMock)
    }

    def "getMockData should return mock data with its payload if there is no binary data"() {
        given: "mock data is returned on the second key with no binary data"
            KEYS.eachWithIndex { String entry, int i ->
                def returnedCustomMockData = i == 0 ? null : MOCK_DATA
                1 * redisSvcMock.popListValue(RedisKeyPrefix.EXECUTE, entry, _) >> returnedCustomMockData
            }
            2 * redisSvcMock.popListValue(RedisKeyPrefix.BINARY, _, _) >> { arg ->
                def params = arg as List
                assert params[1] == KEYS[0]
                null
            } >> { arg ->
                def params = arg as List
                assert params[1] == KEYS[1]
                null
            }
            0 * defaultMockDataRetrieverMock.getDefaultMockData(_, _)

        when: "getMockData is invoked"
            def result = sut.getMockData(KEYS, requestMock, PAYLOAD)

        then: "the mocked data should be returned"
            result.mockData.responseBody == MOCK_DATA.responseBody
            result.redisKey == KEYS[1]
        and: "the thread should delay for the time specified by the mock"
            1 * sleepMock.forMillis(MOCK_DATA.responseSetUpMetadata.delayMs)
    }

    def "getMockData should return mock data with the binary data if it exists"() {
        given: "mock data is returned on the second key with binary data"
            KEYS.eachWithIndex { String entry, int i ->
                def returnedCustomMockData = i == 0 ? null : MOCK_DATA
                1 * redisSvcMock.popListValue(RedisKeyPrefix.EXECUTE, entry, _) >> returnedCustomMockData
            }
            2 * redisSvcMock.popListValue(RedisKeyPrefix.BINARY, _, _) >> { arg ->
                def params = arg as List
                assert params[1] == KEYS[0]
                BINARY
            } >> { arg ->
                def params = arg as List
                assert params[1] == KEYS[1]
                BINARY
            }
            0 * defaultMockDataRetrieverMock.getDefaultMockData(_, _)

        when: "getMockData is invoked"
            def result = sut.getMockData(KEYS, requestMock, PAYLOAD)

        then: "the binary data should be returned"
            result.mockData.responseBody == BINARY
            result.redisKey == KEYS[1]
    }

    def "getMockData should return default data if no mocks exist"() {
        given: "no mock data is returned"
            KEYS.eachWithIndex { String entry, int i ->
                1 * redisSvcMock.popListValue(RedisKeyPrefix.EXECUTE, entry, _) >> null
            }
            2 * redisSvcMock.popListValue(RedisKeyPrefix.BINARY, _, _) >> null
            1 * defaultMockDataRetrieverMock.getDefaultMockData(requestMock, PAYLOAD) >> DEFAULT_MOCK_DATA

        when: "getMockData is invoked"
            def result = sut.getMockData(KEYS, requestMock, PAYLOAD)

        then: "the default mock data should be returned"
            result.mockData.responseBody == DEFAULT_MOCK_DATA.responseBody
            // default mock data does not use Redis
            result.redisKey == null
    }
}
