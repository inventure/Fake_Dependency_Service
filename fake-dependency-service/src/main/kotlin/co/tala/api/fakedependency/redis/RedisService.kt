package co.tala.api.fakedependency.redis

import co.tala.api.fakedependency.configuration.redis.RedisConfigProperties
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.ListOperations
import org.springframework.data.redis.core.RedisOperations
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class RedisService(
    private val redisOperations: RedisOperations<Any, Any>,
    private val mapper: ObjectMapper,
    redisConfig: RedisConfigProperties
) : IRedisService {
    private val ttl = redisConfig.ttl

    override fun <T: Any> setValue(key: String, hKey: String, value: T) {
        val any = mapper.convertValue(value, object : TypeReference<Any>() {})
        redisOperations.opsForHash<String, Any>().put(key, hKey, any)
        redisOperations.expire(key, ttl, TimeUnit.SECONDS)
    }

    override fun <T: Any> getValue(key: String, hKey: String, type: TypeReference<T>): T? {
        val any: Any? = redisOperations.opsForHash<String, Any>().get(key, hKey)
        return mapper.convertValue(any, type)
    }

    override fun <T: Any> pushListValue(key: String, hKey: String, value: T) {
        val listKey = "$key-$hKey"
        val opsForList: ListOperations<Any, Any> = redisOperations.opsForList()
        opsForList.rightPush(listKey, value)
        redisOperations.expire(listKey, ttl, TimeUnit.SECONDS)
    }

    override fun <T: Any> getListValues(key: String, hKey: String, type: TypeReference<List<T>>): List<T> {
        val listKey = "$key-$hKey"
        val opsForList: ListOperations<Any, Any> = redisOperations.opsForList()
        return mapper.convertValue(
            opsForList.range(listKey, 0, opsForList.size(listKey) ?: 0),
            type
        )
    }
}
