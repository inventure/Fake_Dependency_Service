package co.tala.api.fakedependency.business.composer


import co.tala.api.fakedependency.redis.IRedisService
import co.tala.api.fakedependency.redis.RedisKey
import com.fasterxml.jackson.core.type.TypeReference
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest
import co.tala.api.fakedependency.business.parser.IQueryParser
import co.tala.api.fakedependency.business.helper.IKeyHelper
import co.tala.api.fakedependency.business.parser.IPayloadParser

class HKeyWithQueryComposerSpec extends Specification {
    private static final KEYS = ["key1", "key2"]
    private static final QUERY_2_MAP = ["num": "5", "animal": "dog"]
    private static final QUERY_1_MAP = ["num": "5"]
    private static final QUERY_EMPTY_MAP = [:]

    private HttpServletRequest requestMock
    private String requestId
    private IRedisService redisSvcMock
    private IHKeyComposer hKeyComposerMock
    private IQueryParser queryParserMock
    private IKeyHelper keyHelperMock
    private IPayloadParser payloadParserMock
    private IHKeyWithQueryComposer sut

    def setup() {
        requestId = UUID.randomUUID().toString()
        requestMock = Mock()
        redisSvcMock = Mock()
        hKeyComposerMock = Mock()
        queryParserMock = Mock()
        keyHelperMock = Mock()
        payloadParserMock = Mock()
        sut = new HKeyWithQueryComposer(redisSvcMock, hKeyComposerMock, queryParserMock, keyHelperMock, payloadParserMock)
    }

    def "query values should come from uri if they exist and are in Redis"() {
        given: "the same map of 2 entries exists in Redis and the request query"
            def payload = "some payload"
            1 * hKeyComposerMock.getKeys(requestId, requestMock) >> KEYS
            2 * queryParserMock.getQuery(requestMock) >> QUERY_2_MAP
            2 * redisSvcMock.getValue(RedisKey.QUERY_KEY, _, _ as TypeReference) >> { arg ->
                def params = arg as List
                assert params[0].toString() == RedisKey.QUERY_KEY
                assert params[1].toString() == KEYS[0]
                QUERY_2_MAP
            } >> { arg ->
                def params = arg as List
                assert params[0].toString() == RedisKey.QUERY_KEY
                assert params[1].toString() == KEYS[1]
                QUERY_2_MAP
            }

            2 * keyHelperMock.concatenateKeys(*_) >> { arg ->
                def params = arg[0] as List<String>
                assert params[0] == KEYS[0]
                assert params[1] == "5"
                assert params[2] == "dog"
                "result0"
            } >> { arg ->
                def params = arg[0] as List<String>
                assert params[0] == KEYS[1]
                assert params[1] == "5"
                assert params[2] == "dog"
                "result1"
            }
            0 * payloadParserMock.parse(payload, _)

        when:
            def result = sut.getKeys(requestId, requestMock, payload)

        then:
            result == ["result0", "result1"]

    }

    def "query values should be the intersect of the query in Redis and the uri"() {
        given: "Redis has a map of 1 entry, and the query has 2 entries"
            def payload = "some payload"
            1 * hKeyComposerMock.getKeys(requestId, requestMock) >> KEYS
            2 * queryParserMock.getQuery(requestMock) >> QUERY_2_MAP
            2 * redisSvcMock.getValue(RedisKey.QUERY_KEY, _, _ as TypeReference) >> { arg ->
                def params = arg as List
                assert params[0].toString() == RedisKey.QUERY_KEY
                assert params[1].toString() == KEYS[0]
                QUERY_1_MAP
            } >> { arg ->
                def params = arg as List
                assert params[0].toString() == RedisKey.QUERY_KEY
                assert params[1].toString() == KEYS[1]
                QUERY_1_MAP
            }
            2 * keyHelperMock.concatenateKeys(*_) >> { arg ->
                def params = arg[0] as List<String>
                assert params[0] == KEYS[0]
                assert params[1] == "5"
                "result0"
            } >> { arg ->
                def params = arg[0] as List<String>
                assert params[0] == KEYS[1]
                assert params[1] == "5"
                "result1"
            }
            0 * payloadParserMock.parse(payload, _)

        when: "get keys is called"
            def result = sut.getKeys(requestId, requestMock, payload)

        then: "there are 2 results"
            result == ["result0", "result1"]
    }

    def "query values should come from the payload if the uri has no query params"() {
        given: "Redis has a map of 2 query entries, but the uri has none"
            def payload = "some payload"
            1 * hKeyComposerMock.getKeys(requestId, requestMock) >> KEYS
            2 * queryParserMock.getQuery(requestMock) >> QUERY_EMPTY_MAP
            2 * redisSvcMock.getValue(RedisKey.QUERY_KEY, _, _ as TypeReference) >> { arg ->
                def params = arg as List
                assert params[0].toString() == RedisKey.QUERY_KEY
                assert params[1].toString() == KEYS[0]
                QUERY_2_MAP
            } >> { arg ->
                def params = arg as List
                assert params[0].toString() == RedisKey.QUERY_KEY
                assert params[1].toString() == KEYS[1]
                QUERY_2_MAP
            }
            2 * keyHelperMock.concatenateKeys(*_) >> { arg ->
                def params = arg[0] as List<String>
                assert params[0] == KEYS[0]
                assert params[1] == "5"
                assert params[2] == "dog"
                "result0"
            } >> { arg ->
                def params = arg[0] as List<String>
                assert params[0] == KEYS[1]
                assert params[1] == "5"
                assert params[2] == "dog"
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
            1 * hKeyComposerMock.getKeys(requestId, requestMock) >> KEYS
            2 * queryParserMock.getQuery(requestMock) >> QUERY_EMPTY_MAP
            2 * redisSvcMock.getValue(RedisKey.QUERY_KEY, _, _ as TypeReference) >> busSvcMockedResult
            2 * keyHelperMock.concatenateKeys(*_) >>> ["result0", "result1"]
            0 * payloadParserMock.parse(payload, _)

        when:
            def result = sut.getKeys(requestId, requestMock, payload)

        then:
            result == ["result0", "result1"]

        where:
            busSvcMockedResult << [QUERY_EMPTY_MAP, null]
    }

}
