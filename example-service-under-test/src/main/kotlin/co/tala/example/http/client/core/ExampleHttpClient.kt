package co.tala.example.http.client.core

import co.tala.example.http.client.core.ExampleHttpMethod.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.Instant


interface IExampleHttpClient {
    fun post(
        uri: String,
        headers: Map<String, String>,
        content: Any?
    ): RawResponse

    fun put(
        uri: String,
        headers: Map<String, String>,
        content: Any?
    ): RawResponse

    fun patch(
        uri: String,
        headers: Map<String, String>,
        content: Any?
    ): RawResponse

    fun get(
        uri: String,
        headers: Map<String, String>
    ): RawResponse

    fun delete(
        uri: String,
        headers: Map<String, String>
    ): RawResponse
}

class ExampleHttpClient(
    private val okHttpClient: OkHttpClient,
    private val baseUrl: String,
    private val gson: Gson
) : IExampleHttpClient {

    override fun post(
        uri: String,
        headers: Map<String, String>,
        content: Any?
    ): RawResponse = send(
        method = POST,
        uri = uri,
        headers = headers,
        content = content
    )

    override fun put(
        uri: String,
        headers: Map<String, String>,
        content: Any?
    ): RawResponse = send(
        method = PUT,
        uri = uri,
        headers = headers,
        content = content
    )

    override fun patch(
        uri: String,
        headers: Map<String, String>,
        content: Any?
    ): RawResponse = send(
        method = PATCH,
        uri = uri,
        headers = headers,
        content = content
    )

    override fun get(
        uri: String,
        headers: Map<String, String>
    ): RawResponse = send(
        method = GET,
        uri = uri,
        headers = headers,
        content = null
    )

    override fun delete(
        uri: String,
        headers: Map<String, String>
    ): RawResponse = send(
        method = DELETE,
        uri = uri,
        headers = headers,
        content = null
    )

    private fun send(
        method: ExampleHttpMethod,
        uri: String,
        headers: Map<String, String>,
        content: Any?
    ): RawResponse {
        val url = "$baseUrl$uri"
        val requestBody: RequestBody = when {
            content != null -> when (content) {
                is ByteArray -> content.toRequestBody()
                is String -> content.toRequestBody()
                else -> gson.toJson(content).toRequestBody()
            }
            else -> "".toRequestBody()
        }
        val contentHeader: Pair<String, String>? = when {
            content != null && content !is String -> "Content-Type" to when (content) {
                is ByteArray -> "application/octet-stream"
                else -> "application/json"
            }
            else -> null
        }

        val request: Request = Request.Builder()
            .url(url)
            .also { builder ->
                headers.forEach {
                    builder.addHeader(it.key, it.value)
                }
                if (contentHeader != null) builder.addHeader(contentHeader.first, contentHeader.second)
            }
            .also { builder ->
                when (method) {
                    POST -> builder.post(requestBody)
                    PUT -> builder.put(requestBody)
                    PATCH -> builder.patch(requestBody)
                    GET -> builder.get()
                    DELETE -> builder.delete()
                }
            }
            .build()

        val start = Instant.now()
        val response = okHttpClient.newCall(request).execute()
        val end = Instant.now()

        return RawResponse(
            gson = gson,
            okHttpResponse = response,
            start = start,
            end = end
        )
    }

}

inline fun <reified T : Any> RawResponse.apiResponse(): ApiResponse<T> {
    val responseBytes = okHttpResponse.body?.byteStream()?.readAllBytes()
    val responseString = if (responseBytes != null) String(responseBytes) else null
    val deserializedResponse: T? = when {
        responseString != null -> try {
            gson.fromJson<T>(responseString, object : TypeToken<T>() {}.type)
        } catch (e: Exception) {
            null
        }
        else -> null
    }

    return ApiResponse(
        body = deserializedResponse,
        responseString = responseString,
        statusCode = okHttpResponse.code,
        start = start,
        end = end,
        headers = okHttpResponse.headers.toMultimap(),
        url = okHttpResponse.request.url.toUrl().toString(),
        method = ExampleHttpMethod.valueOf(okHttpResponse.request.method.uppercase())
    )
}
