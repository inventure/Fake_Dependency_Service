package co.tala.api.fakedependency.redis

import co.tala.api.fakedependency.configuration.redis.RedisConfigProperties
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.ListOperations
import org.springframework.data.redis.core.RedisOperations
import org.springframework.data.redis.core.SetOperations
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class RedisService(
    private val redisOperations: RedisOperations<Any, Any>,
    private val mapper: ObjectMapper,
    redisConfig: RedisConfigProperties
) : IRedisService {
    private val ttl = redisConfig.ttl

    override fun <T : Any> setValue(keyPrefix: String, key: String, value: T?) {
        val valueKey = "$keyPrefix-value-$key"
        val any = mapper.convertValue(value, object : TypeReference<Any>() {})
        redisOperations.opsForValue().set(valueKey, any)
        redisOperations.expire(valueKey, ttl, TimeUnit.SECONDS)
    }

    override fun <T : Any> getValue(keyPrefix: String, key: String, type: TypeReference<T>): T? {
        val valueKey = "$keyPrefix-value-$key"
        val any: Any? = redisOperations.opsForValue().get(valueKey)
        return if (any != null) mapper.convertValue(any, type) else null
    }

    override fun <T : Any> pushListValue(keyPrefix: String, key: String, value: T?) {
        val listKey = "$keyPrefix-list-$key"
        val any = mapper.convertValue(value, object : TypeReference<Any>() {})
        val opsForList: ListOperations<Any, Any> = redisOperations.opsForList()
        opsForList.rightPush(listKey, any)
        redisOperations.expire(listKey, ttl, TimeUnit.SECONDS)
        // Store last value to be used in case popping list yields empty
        setValue(keyPrefix, key, value)
    }

    override fun <T : Any> popListValue(keyPrefix: String, key: String, type: TypeReference<T>): T? {
        val listKey = "$keyPrefix-list-$key"
        val opsForList: ListOperations<Any, Any> = redisOperations.opsForList()
        val any: Any? = opsForList.leftPop(listKey)
        // If list is empty or not found, then try from getValue, which stores the last mocked response
        return mapper.convertValue(any, type) ?: getValue(keyPrefix, key, type)
    }

    override fun <T : Any> getListValues(keyPrefix: String, key: String, type: TypeReference<List<T>>): List<T> {
        val listKey = "$keyPrefix-list-$key"
        val opsForList: ListOperations<Any, Any> = redisOperations.opsForList()
        return mapper.convertValue(
            opsForList.range(listKey, 0, opsForList.size(listKey) ?: 0),
            type
        )
    }

    override fun <T : Any> pushSetValue(keyPrefix: String, key: String, value: T) {
        val setKey = "$keyPrefix-set-$key"
        val opsForSet: SetOperations<Any, Any> = redisOperations.opsForSet()
        opsForSet.add(setKey, value)
        redisOperations.expire(setKey, ttl, TimeUnit.SECONDS)
    }

    override fun <T : Any> getSetValues(keyPrefix: String, key: String, type: TypeReference<Set<T>>): Set<T> {
        val setKey = "$keyPrefix-set-$key"
        val opsForSet: SetOperations<Any, Any> = redisOperations.opsForSet()
        val members: Set<Any>? = opsForSet.members(setKey)
        return if (members != null) mapper.convertValue(members, type) else emptySet()
    }
}
