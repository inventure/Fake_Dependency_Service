package co.tala.example.http.client.lib.builder

import com.google.gson.Gson
import java.net.URLEncoder
import java.time.Instant
import java.time.format.DateTimeFormatter


interface IQueryParamBuilder {
    fun addParam(key: String, value: String?): IQueryParamBuilder
    fun addParam(key: String, value: Double?): IQueryParamBuilder
    fun addParam(key: String, value: List<String>?): IQueryParamBuilder
    fun addInstantParams(key: String, value: List<Instant>?): IQueryParamBuilder
    fun addParam(key: String, value: Instant?): IQueryParamBuilder
    fun addParam(key: String, value: Int?): IQueryParamBuilder
    fun addParam(key: String, value: Long?): IQueryParamBuilder
    fun addSerializedParam(key: String, value: Any): IQueryParamBuilder
    fun addSerializedParam(key: String, value: List<Any>): IQueryParamBuilder
    fun clear(): IQueryParamBuilder
    fun build(): String
}

class QueryParamBuilder(
    private val gson: Gson,
    private val instantFormatter: DateTimeFormatter = DateTimeFormatter.ISO_INSTANT
) : IQueryParamBuilder {
    private val params: MutableMap<String, List<String>> = mutableMapOf()

    override fun addParam(key: String, value: Long?): IQueryParamBuilder = apply {
        if (value != null)
            params[key] = listOf(value.toString())
    }

    override fun addSerializedParam(key: String, value: Any): IQueryParamBuilder = apply {
        params[key] = listOf(gson.toJson(value))
    }

    override fun addSerializedParam(key: String, value: List<Any>): IQueryParamBuilder = apply {
        params[key] = value.map { URLEncoder.encode(gson.toJson(it), "UTF-8") }
    }

    override fun addParam(key: String, value: Int?): IQueryParamBuilder = apply {
        if (value != null)
            params[key] = listOf(value.toString())
    }

    override fun addParam(key: String, value: Instant?): IQueryParamBuilder = apply {
        if (value != null)
            params[key] = listOf(instantFormatter.format(value))
    }

    override fun addParam(key: String, value: String?): IQueryParamBuilder = apply {
        if (value != null)
            params[key] = listOf(value)
    }

    override fun addParam(key: String, value: Double?): IQueryParamBuilder = apply {
        if (value != null)
            params[key] = listOf(value.toString())
    }

    override fun addParam(key: String, value: List<String>?): IQueryParamBuilder = apply {
        if (value != null)
            params[key] = value
    }

    override fun addInstantParams(key: String, value: List<Instant>?): IQueryParamBuilder = apply {
        if (value != null)
            params[key] = value.map { instantFormatter.format(it) }
    }

    override fun clear(): IQueryParamBuilder = apply {
        return QueryParamBuilder(gson, instantFormatter)
    }

    override fun build(): String {
        val sb = StringBuilder()
        val appendAmpersand: () -> Unit = { if (sb.length > 1) sb.append("&") }
        val appendParam: (String, String) -> Unit = { key: String, value: String -> sb.append("$key=$value") }

        sb.append("?")
        params.forEach { param ->
            param.value.forEach { value ->
                appendAmpersand()
                appendParam(param.key, value)
            }
        }
        return if (sb.length > 1) sb.toString() else ""
    }
}
