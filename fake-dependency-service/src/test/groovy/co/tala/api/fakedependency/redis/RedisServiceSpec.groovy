package co.tala.api.fakedependency.redis

import co.tala.api.fakedependency.configuration.jackson.JacksonConfiguration
import co.tala.api.fakedependency.configuration.redis.RedisConfigProperties
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.ListOperations
import org.springframework.data.redis.core.RedisOperations
import org.springframework.data.redis.core.SetOperations
import org.springframework.data.redis.core.ValueOperations
import spock.lang.Specification

import java.util.concurrent.TimeUnit

class RedisServiceSpec extends Specification {
    private static final RedisConfigProperties CONFIG = new RedisConfigProperties(1000L, "hostname", 9999)
    private static final ObjectMapper MAPPER = new JacksonConfiguration().mapper()
    private static final String KEY_PREFIX = "some key prefix"
    private static final String KEY = "some key"
    private static final LinkedHashMap<String, String> OBJ = ["someMapKey": "someMapValue"]

    private RedisOperations redisOperationsMock
    private ListOperations listOperationsMock
    private ValueOperations valueOperationsMock
    private SetOperations setOperationsMock
    private IRedisService sut

    def setup() {
        redisOperationsMock = Mock()
        listOperationsMock = Mock()
        valueOperationsMock = Mock()
        setOperationsMock = Mock()
        redisOperationsMock.opsForList() >> listOperationsMock
        redisOperationsMock.opsForValue() >> valueOperationsMock
        redisOperationsMock.opsForSet() >> setOperationsMock
        sut = new RedisService(redisOperationsMock, MAPPER, CONFIG)
    }

    def "setValue should put value into Redis with expiration ttl"() {
        when: "setValue is invoked"
            sut.setValue(KEY_PREFIX, KEY, OBJ)

        then: "the value should be put into Redis"
            1 * valueOperationsMock.set("$KEY_PREFIX-value-$KEY", _) >> { args ->
                assert args[1] as LinkedHashMap<String, String> == OBJ
            }
        and: "the expiration should be set"
            1 * redisOperationsMock.expire("$KEY_PREFIX-value-$KEY", CONFIG.ttl, TimeUnit.SECONDS)

    }

    def "getValue should the value from Redis"() {
        given: "redis will return an object"
            1 * valueOperationsMock.get("$KEY_PREFIX-value-$KEY") >> OBJ

        when: "getValue is invoked"
            Object result = sut.getValue(KEY_PREFIX, KEY, new TypeReference<Object>() {})

        then: "the correct value should be returned"
            result == OBJ
    }

    def "pushListValue should put value into Redis List with expiration ttl and call setValue"() {
        given:
            String listKey = "$KEY_PREFIX-list-$KEY"

        when: "pushListValue is invoked"
            sut.pushListValue(KEY_PREFIX, KEY, OBJ)

        then: "the value should be put into Redis List"
            1 * listOperationsMock.rightPush(listKey, _) >> { args ->
                assert args[1] as LinkedHashMap<String, String> == OBJ
            }
        and: "the expiration should be set"
            1 * redisOperationsMock.expire(listKey, CONFIG.ttl, TimeUnit.SECONDS)

        and: "the value should be put into Redis"
            1 * valueOperationsMock.set("$KEY_PREFIX-value-$KEY", _) >> { args ->
                assert args[1] as LinkedHashMap<String, String> == OBJ
            }

    }

    def "popListValue should return a left pop from Redis"() {
        given: "redis will return a list of objects"
            String listKey = "$KEY_PREFIX-list-$KEY"
            1 * listOperationsMock.leftPop(listKey) >> OBJ

        when: "popListValue is invoked"
            Object result = sut.popListValue(KEY_PREFIX, KEY, new TypeReference<Object>() {})

        then: "the correct value should be returned"
            result == OBJ
    }

    def "popListValue should call getValue if left pop returns null"() {
        given: "redis will return a list of objects"
            String listKey = "$KEY_PREFIX-list-$KEY"
            1 * listOperationsMock.leftPop(listKey) >> null
            1 * valueOperationsMock.get("$KEY_PREFIX-value-$KEY") >> OBJ

        when: "popListValue is invoked"
            Object result = sut.popListValue(KEY_PREFIX, KEY, new TypeReference<Object>() {})

        then: "the correct value should be returned"
            result == OBJ
    }

    def "getListValues should return a list of objects from Redis"() {
        given: "redis will return a list of objects"
            String listKey = "$KEY_PREFIX-list-$KEY"
            ArrayList<LinkedHashMap<String, String>> objects = [OBJ, OBJ]
            1 * listOperationsMock.size(listKey) >> 2
            1 * listOperationsMock.range(listKey, 0, 2) >> objects

        when: "getListValues is invoked"
            Object result = sut.getListValues(KEY_PREFIX, KEY, new TypeReference<List<Object>>() {})

        then: "the correct value should be returned"
            result == objects
    }

    def "getListValues should return an empty list from Redis"() {
        given: "redis will return a list of objects"
            String listKey = "$KEY_PREFIX-list-$KEY"
            ArrayList<LinkedHashMap<String, String>> objects = []
            1 * listOperationsMock.size(listKey) >> 0
            1 * listOperationsMock.range(listKey, 0, 0) >> objects

        when: "getListValues is invoked"
            Object result = sut.getListValues(KEY_PREFIX, KEY, new TypeReference<List<Object>>() {})

        then: "the correct value should be returned"
            result == objects
    }

    def "pushSetValue should put value into Redis List with expiration ttl"() {
        given:
            String setKey = "$KEY_PREFIX-set-$KEY"

        when: "pushSetValue is invoked"
            sut.pushSetValue(KEY_PREFIX, KEY, OBJ)

        then: "the value should be put into Redis List"
            1 * setOperationsMock.add(setKey, _) >> { args ->
                assert args[1] as LinkedHashMap<String, String> == OBJ
            }
        and: "the expiration should be set"
            1 * redisOperationsMock.expire(setKey, CONFIG.ttl, TimeUnit.SECONDS)

    }

    def "getSetValues should return a set of objects from Redis"() {
        given: "redis will return a list of objects"
            String setKey = "$KEY_PREFIX-set-$KEY"
            Set<LinkedHashMap<String, String>> objects = [OBJ, OBJ]
            1 * setOperationsMock.members(setKey) >> objects

        when: "getSetValues is invoked"
            Object result = sut.getSetValues(KEY_PREFIX, KEY, new TypeReference<Set<Object>>() {})

        then: "the correct value should be returned"
            result == objects
    }

    def "getSetValues should return an empty list from Redis"() {
        given: "redis will return a list of objects"
            String setKey = "$KEY_PREFIX-set-$KEY"
            1 * setOperationsMock.members(setKey) >> null

        when: "getSetValues is invoked"
            Object result = sut.getSetValues(KEY_PREFIX, KEY, new TypeReference<Set<Object>>() {})

        then: "the correct value should be returned"
            result == [] as Set<Object>
    }
}
