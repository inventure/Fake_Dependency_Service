package co.tala.example.http.client.core

import co.tala.example.http.client.core.ExampleHttpMethod.*
import co.tala.example.http.client.core.request.BinaryBodyRequest
import co.tala.example.http.client.core.request.FormBodyRequest
import co.tala.example.http.client.core.request.MultipartBodyRequest
import co.tala.example.http.client.lib.builder.IRequestHeaderBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.Instant


interface IExampleHttpClient {
    fun post(
        uri: String,
        headers: Map<String, String> = emptyMap(),
        content: Any? = null
    ): RawResponse

    fun put(
        uri: String,
        headers: Map<String, String> = emptyMap(),
        content: Any? = null
    ): RawResponse

    fun patch(
        uri: String,
        headers: Map<String, String> = emptyMap(),
        content: Any? = null
    ): RawResponse

    fun get(
        uri: String,
        headers: Map<String, String> = emptyMap()
    ): RawResponse

    fun delete(
        uri: String,
        headers: Map<String, String> = emptyMap()
    ): RawResponse
}

class ExampleHttpClient(
    private val okHttpClient: OkHttpClient,
    private val requestHeaderBuilder: IRequestHeaderBuilder,
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
            content != null -> {
                when (content) {
                    // For binary content, such as file uploads
                    is BinaryBodyRequest -> content.toRequestBody()
                    // For binary content, content-type defaults to application/octet-stream for ByteArray
                    is ByteArray -> content.toRequestBody(ContentType.APPLICATION_OCTET_STREAM.value.toMediaType())
                    // for string content, such as XML
                    is String -> content.toRequestBody(ContentType.TEXT_PLAIN.value.toMediaType())
                    // for multi-part content, such as multipart/form-data
                    is MultipartBodyRequest -> content.toRequestBody()
                    // for application/x-www-form-urlencoded content
                    // media type of application/x-www-form-urlencoded is set automatically by okHttp
                    is FormBodyRequest -> content.toRequestBody()
                    // else we expect json content
                    else -> gson.toJson(content).toRequestBody(ContentType.APPLICATION_JSON.value.toMediaType())
                }
            }
            else -> "".toRequestBody()
        }

        val request: Request = Request.Builder()
            .url(url)
            .also { builder ->
                requestHeaderBuilder
                    .clear()
                    .build()
                    .plus(headers)
                    .forEach {
                        builder.addHeader(it.key, it.value)
                    }
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
    val isByteArray = T::class.java == ByteArray::class.java
    val responseString =
        if (responseBytes != null && !isByteArray) String(responseBytes) else null
    val deserializedResponse: T? = when {
        isByteArray -> responseBytes as T
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
