package co.tala.api.fakedependency.business.composer

import co.tala.api.fakedependency.business.helper.IKeyHelper
import co.tala.api.fakedependency.business.helper.IRequestExtractor
import co.tala.api.fakedependency.business.parser.IPayloadParser
import co.tala.api.fakedependency.business.parser.IQueryParser
import co.tala.api.fakedependency.redis.IRedisService
import co.tala.api.fakedependency.redis.RedisKeyPrefix
import co.tala.api.fakedependency.redis.RedisOpsType
import com.fasterxml.jackson.core.type.TypeReference
import spock.lang.Specification
import spock.lang.Unroll

import javax.servlet.http.HttpServletRequest

@Unroll
class RedisKeyWithQueryComposerSpec extends Specification {
    private static final KEYS = ["key1", "key2"]
    private static final Map<String, List<String>> QUERY_MAP = ["animal": ["dog", "cat"], "num": ["5"]]
    private static final Set<String> QUERY_KEY_SET = ["num", "animal"] // test the set order should be sorted
    private static final Map<String, List<String>> QUERY_EMPTY_MAP = [:]
    private static final List<String> RESULTS = ["result0", "result1"]

    private HttpServletRequest requestMock
    private String requestId
    private IRedisService redisSvcMock
    private IRedisKeyComposer redisKeyComposerMock
    private IQueryParser queryParserMock
    private IKeyHelper keyHelperMock
    private IPayloadParser payloadParserMock
    private IRequestExtractor requestExtractorMock
    private IRedisKeyWithQueryComposer sut

    def setup() {
        requestId = UUID.randomUUID().toString()
        requestMock = Mock()
        redisSvcMock = Mock()
        redisKeyComposerMock = Mock()
        queryParserMock = Mock()
        keyHelperMock = Mock()
        payloadParserMock = Mock()
        requestExtractorMock = Mock()
        sut = new RedisKeyWithQueryComposer(redisSvcMock, redisKeyComposerMock, queryParserMock, keyHelperMock, payloadParserMock, requestExtractorMock)
    }

    def "query values should come from uri if they exist and are in Redis"() {
        given: "the same map of 2 entries exists in Redis and the request query"
            def payload = "some payload"
            1 * redisKeyComposerMock.getKeys(requestId, requestMock, true) >> KEYS
            2 * queryParserMock.getQuery(requestMock) >> QUERY_MAP
            0 * redisSvcMock.getSetValues(RedisKeyPrefix.QUERY, _, _ as TypeReference) >> { arg ->
                def params = arg as List
                assert params[0].toString() == RedisKeyPrefix.QUERY
                assert params[1].toString() == KEYS[0]
                QUERY_KEY_SET
            } >> { arg ->
                def params = arg as List
                assert params[0].toString() == RedisKeyPrefix.QUERY
                assert params[1].toString() == KEYS[1]
                QUERY_KEY_SET
            }
            0 * requestExtractorMock.getPayloadFromRequestHeaders(_, requestMock) >> null
            2 * keyHelperMock.concatenateKeys(*_) >> { arg ->
                def params = arg[0] as List<String>
                assert params == [KEYS[0], "animal", "num", ["dog", "cat"], ["5"]]
                RESULTS[0]
            } >> { arg ->
                def params = arg[0] as List<String>
                assert params == [KEYS[1], "animal", "num", ["dog", "cat"], ["5"]]
                "result1"
            }
            RESULTS.each {
                1 * redisSvcMock.hasKey(RedisKeyPrefix.EXECUTE, RedisOpsType.VALUE, it) >> true
            }
            0 * payloadParserMock.parse(payload, _)

        when:
            def result = sut.getKeys(requestId, requestMock, payload)

        then:
            result == [RESULTS[0], RESULTS[1]]
    }

    def "query values should come from the payload if the uri has no query params"() {
        given: "Redis has a map of 2 query entries, but the uri has none"
            def payload = "some payload"
            String invalidKey = "invalidKey"
            1 * redisKeyComposerMock.getKeys(requestId, requestMock, true) >> KEYS
            2 * queryParserMock.getQuery(requestMock) >> QUERY_EMPTY_MAP
            2 * redisSvcMock.getSetValues(RedisKeyPrefix.QUERY, _, _ as TypeReference) >> { arg ->
                def params = arg as List
                assert params[0].toString() == RedisKeyPrefix.QUERY
                assert params[1].toString() == KEYS[0]
                QUERY_KEY_SET
            } >> { arg ->
                def params = arg as List
                assert params[0].toString() == RedisKeyPrefix.QUERY
                assert params[1].toString() == KEYS[1]
                QUERY_KEY_SET
            }
            2 * requestExtractorMock.getPayloadFromRequestHeaders(_, requestMock) >> payloadOverride
            2 * redisSvcMock.hasKey(RedisKeyPrefix.EXECUTE, RedisOpsType.VALUE, invalidKey) >> false
            4 * keyHelperMock.concatenateKeys(*_) >> invalidKey >> { arg ->
                def params = arg[0] as List<String>
                assert params == [KEYS[0], "animal", "num", ["dog"], ["5"]]
                RESULTS[0]
            } >> invalidKey >> { arg ->
                def params = arg[0] as List<String>
                assert params == [KEYS[1], "animal", "num", ["dog"], ["5"]]
                RESULTS[1]
            }
            // if the payload override is set, then it should parse that
            // else it should parse the actual payload
            4 * payloadParserMock.parse(payloadOverride ?: payload, _) >> { arg ->
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
            } >> { arg ->
                def params = arg as List
                assert params[1] == "num"
                "5"
            }

        when: "get keys is called"
            def result = sut.getKeys(requestId, requestMock, payload)

        then: "there are 2 results"
            result == [RESULTS[0], RESULTS[1]]

        where:
            payloadOverride << [null, "some payload override"]

    }

    def "query values should be empty if the uri has no query params and the payload is null"() {
        given: "the payload is null, redis returns no values, and the query is empty in the uri"
            def payload = null
            1 * redisKeyComposerMock.getKeys(requestId, requestMock, true) >> KEYS
            2 * queryParserMock.getQuery(requestMock) >> QUERY_EMPTY_MAP
            0 * redisSvcMock.getSetValues(RedisKeyPrefix.QUERY, _, _ as TypeReference) >> QUERY_KEY_SET
            RESULTS.each {
                1 * redisSvcMock.hasKey(RedisKeyPrefix.EXECUTE, RedisOpsType.VALUE, it) >> true
            }
            2 * keyHelperMock.concatenateKeys(*_) >>> [RESULTS[0], RESULTS[1]]
            0 * payloadParserMock.parse(payload, _)

        when:
            def result = sut.getKeys(requestId, requestMock, payload)

        then:
            result == [RESULTS[0], RESULTS[1]]
    }

}
