package co.tala.api.fakedependency.redis

import co.tala.api.fakedependency.configuration.jackson.JacksonConfiguration
import co.tala.api.fakedependency.configuration.redis.RedisConfigProperties
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.HashOperations
import org.springframework.data.redis.core.ListOperations
import org.springframework.data.redis.core.RedisOperations
import spock.lang.Specification

import java.util.concurrent.TimeUnit

class RedisServiceSpec extends Specification {
    private static final RedisConfigProperties CONFIG = new RedisConfigProperties(1000L, "hostname", 9999)
    private static final ObjectMapper MAPPER = new JacksonConfiguration().mapper()
    private static final String KEY = "some key"
    private static final String HKEY = "some hash key"
    private static final LinkedHashMap<String, String> OBJ = ["someMapKey": "someMapValue"]

    private RedisOperations redisOperationsMock
    private HashOperations hashOperationsMock
    private ListOperations listOperationsMock
    private IRedisService sut

    def setup() {
        redisOperationsMock = Mock()
        hashOperationsMock = Mock()
        listOperationsMock = Mock()
        redisOperationsMock.opsForHash() >> hashOperationsMock
        redisOperationsMock.opsForList() >> listOperationsMock
        sut = new RedisService(redisOperationsMock, MAPPER, CONFIG)
    }

    def "setValue should put value into Redis with expiration ttl"() {
        when: "setValue is invoked"
            sut.setValue(KEY, HKEY, OBJ)

        then: "the value should be put into Redis"
            1 * hashOperationsMock.put(KEY, HKEY, _) >> { args ->
                assert args[2] as LinkedHashMap<String, String> == OBJ
            }
        and: "the expiration should be set"
            1 * redisOperationsMock.expire(KEY, CONFIG.ttl, TimeUnit.SECONDS)

    }

    def "getValue should the value from Redis"() {
        given: "redis will return an object"
            1 * hashOperationsMock.get(KEY, HKEY) >> OBJ

        when: "getValue is invoked"
            Object result = sut.getValue(KEY, HKEY, new TypeReference<Object>() {})

        then: "the correct value should be returned"
            result == OBJ
    }

    def "pushListValue should put value into Redis List with expiration ttl"() {
        given:
            String listKey = "$KEY-$HKEY"

        when: "pushListValue is invoked"
            sut.pushListValue(KEY, HKEY, OBJ)

        then: "the value should be put into Redis List"
            1 * listOperationsMock.rightPush(listKey, _) >> { args ->
                assert args[1] as LinkedHashMap<String, String> == OBJ
            }
        and: "the expiration should be set"
            1 * redisOperationsMock.expire(listKey, CONFIG.ttl, TimeUnit.SECONDS)

    }

    def "getListValues should return a list of objects from Redis"() {
        given: "redis will return a list of objects"
            String listKey = "$KEY-$HKEY"
            ArrayList<LinkedHashMap<String, String>> objects = [OBJ, OBJ]
            1 * listOperationsMock.size(listKey) >> 2
            1 * listOperationsMock.range(listKey, 0, 2) >> objects

        when: "getListValues is invoked"
            Object result = sut.getListValues(KEY, HKEY, new TypeReference<List<Object>>() {})

        then: "the correct value should be returned"
            result == objects
    }

    def "getListValues should return an empty list from Redis"() {
        given: "redis will return a list of objects"
            String listKey = "$KEY-$HKEY"
            ArrayList<LinkedHashMap<String, String>> objects = []
            1 * listOperationsMock.size(listKey) >> 0
            1 * listOperationsMock.range(listKey, 0, 0) >> objects

        when: "getListValues is invoked"
            Object result = sut.getListValues(KEY, HKEY, new TypeReference<List<Object>>() {})

        then: "the correct value should be returned"
            result == objects
    }
}
