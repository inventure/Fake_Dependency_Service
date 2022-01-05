package co.tala.example.http.client.core

import java.time.Instant

data class ApiResponse<T>(
    val body: T?,
    @Transient
    val responseString: String?,
    val statusCode: Int,
    @Transient
    val start: Instant,
    @Transient
    val end: Instant,
    @Transient
    val headers: Map<String, List<String>>,
    val url: String,
    val method: ExampleHttpMethod
) {
    val isSuccessful: Boolean = statusCode in 200..299
    val elapsed: Long = end.toEpochMilli() - start.toEpochMilli()
}
