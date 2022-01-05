package co.tala.api.fakedependency.redis

import com.fasterxml.jackson.core.type.TypeReference

interface IRedisService {
    fun <T : Any> setValue(key: String, hKey: String, value: T)
    fun <T : Any> getValue(key: String, hKey: String, type: TypeReference<T>): T?
    fun <T : Any> pushListValue(key: String, hKey: String, value: T)
    fun <T : Any> getListValues(key: String, hKey: String, type: TypeReference<List<T>>): List<T>
}
