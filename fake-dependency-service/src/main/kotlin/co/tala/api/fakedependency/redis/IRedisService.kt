package co.tala.api.fakedependency.redis

import com.fasterxml.jackson.core.type.TypeReference

interface IRedisService {
    fun <T : Any> setValue(keyPrefix: String, key: String, value: T?)
    fun <T : Any> getValue(keyPrefix: String, key: String, type: TypeReference<T>): T?
    fun <T : Any> pushListValue(keyPrefix: String, key: String, value: T?)
    fun <T : Any> popListValue(keyPrefix: String, key: String, type: TypeReference<T>): T?
    fun <T : Any> getListValues(keyPrefix: String, key: String, type: TypeReference<List<T>>): List<T>
    fun <T : Any> pushSetValues(keyPrefix: String, key: String, vararg values: T)
    fun <T : Any> getSetValues(keyPrefix: String, key: String, type: TypeReference<Set<T>>): Set<T>
    fun hasKey(keyPrefix: String, opsType: RedisOpsType, key: String): Boolean
}
