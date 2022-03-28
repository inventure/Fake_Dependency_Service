package co.tala.api.fakedependency.business.composer

import co.tala.api.fakedependency.business.helper.IKeyHelper
import co.tala.api.fakedependency.business.parser.IPayloadParser
import co.tala.api.fakedependency.business.parser.IQueryParser
import co.tala.api.fakedependency.redis.IRedisService
import co.tala.api.fakedependency.redis.RedisKeyPrefix
import com.fasterxml.jackson.core.type.TypeReference
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest

class RedisKeyWithQueryComposerSpec extends Specification {
    private static final KEYS = ["key1", "key2"]
    private static final Map<String, List<String>> QUERY_2_MAP = ["num": ["5"], "animal": ["dog", "cat"]]
    private static final Set<Map<String, List<String>>> QUERY_2_SET_MAP = [["num": ["5"], "animal": ["dog", "cat"]]]
    private static final Map<String, List<String>> QUERY_EMPTY_MAP = [:]

    private HttpServletRequest requestMock
    private String requestId
    private IRedisService redisSvcMock
    private IRedisKeyComposer redisKeyComposerMock
    private IQueryParser queryParserMock
    private IKeyHelper keyHelperMock
    private IPayloadParser payloadParserMock
    private IRedisKeyWithQueryComposer sut

    def setup() {
        requestId = UUID.randomUUID().toString()
        requestMock = Mock()
        redisSvcMock = Mock()
        redisKeyComposerMock = Mock()
        queryParserMock = Mock()
        keyHelperMock = Mock()
        payloadParserMock = Mock()
        sut = new RedisKeyWithQueryComposer(redisSvcMock, redisKeyComposerMock, queryParserMock, keyHelperMock, payloadParserMock)
    }

    def "query values should come from uri if they exist and are in Redis"() {
        given: "the same map of 2 entries exists in Redis and the request query"
            def payload = "some payload"
            1 * redisKeyComposerMock.getKeys(requestId, requestMock, true) >> KEYS
            2 * queryParserMock.getQuery(requestMock) >> QUERY_2_MAP
            2 * redisSvcMock.getSetValues(RedisKeyPrefix.QUERY, _, _ as TypeReference) >> { arg ->
                def params = arg as List
                assert params[0].toString() == RedisKeyPrefix.QUERY
                assert params[1].toString() == KEYS[0]
                QUERY_2_SET_MAP
            } >> { arg ->
                def params = arg as List
                assert params[0].toString() == RedisKeyPrefix.QUERY
                assert params[1].toString() == KEYS[1]
                QUERY_2_SET_MAP
            }

            2 * keyHelperMock.concatenateKeys(*_) >> { arg ->
                def params = arg[0] as List<String>
                assert params == [KEYS[0], "num", "animal", ["5"], ["dog", "cat"]]
                "result0"
            } >> { arg ->
                def params = arg[0] as List<String>
                assert params == [KEYS[1], "num", "animal", ["5"], ["dog", "cat"]]
                "result1"
            }
            0 * payloadParserMock.parse(payload, _)

        when:
            def result = sut.getKeys(requestId, requestMock, payload)

        then:
            result == ["result0", "result1"]
    }

    def "query values should come from the payload if the uri has no query params"() {
        given: "Redis has a map of 2 query entries, but the uri has none"
            def payload = "some payload"
            1 * redisKeyComposerMock.getKeys(requestId, requestMock, true) >> KEYS
            2 * queryParserMock.getQuery(requestMock) >> QUERY_EMPTY_MAP
            2 * redisSvcMock.getSetValues(RedisKeyPrefix.QUERY, _, _ as TypeReference) >> { arg ->
                def params = arg as List
                assert params[0].toString() == RedisKeyPrefix.QUERY
                assert params[1].toString() == KEYS[0]
                QUERY_2_SET_MAP
            } >> { arg ->
                def params = arg as List
                assert params[0].toString() == RedisKeyPrefix.QUERY
                assert params[1].toString() == KEYS[1]
                QUERY_2_SET_MAP
            }
            2 * keyHelperMock.concatenateKeys(*_) >> { arg ->
                def params = arg[0] as List<String>
                assert params == [KEYS[0], "num", "animal", ["5"], ["dog"]]
                "result0"
            } >> { arg ->
                def params = arg[0] as List<String>
                assert params == [KEYS[1], "num", "animal", ["5"], ["dog"]]
                "result1"
            }
            4 * payloadParserMock.parse(payload, _) >> { arg ->
                def params = arg as List
                assert params[1] == "num"
                "5"
            } >> { arg ->
                def params = arg as List
                assert params[1] == "animal"
                "dog"
            } >> { arg ->
                def params = arg as List
                assert params[1] == "num"
                "5"
            } >> { arg ->
                def params = arg as List
                assert params[1] == "animal"
                "dog"
            }

        when: "get keys is called"
            def result = sut.getKeys(requestId, requestMock, payload)

        then: "there are 2 results"
            result == ["result0", "result1"]

    }

    def "query values should be empty if the uri has no query params and the payload is null"() {
        given: "the payload is null, redis returns no values, and the query is empty in the uri"
            def payload = null
            1 * redisKeyComposerMock.getKeys(requestId, requestMock, true) >> KEYS
            2 * queryParserMock.getQuery(requestMock) >> QUERY_EMPTY_MAP
            2 * redisSvcMock.getSetValues(RedisKeyPrefix.QUERY, _, _ as TypeReference) >> QUERY_2_SET_MAP
            2 * keyHelperMock.concatenateKeys(*_) >>> ["result0", "result1"]
            0 * payloadParserMock.parse(payload, _)

        when:
            def result = sut.getKeys(requestId, requestMock, payload)

        then:
            result == ["result0", "result1"]
    }

}
