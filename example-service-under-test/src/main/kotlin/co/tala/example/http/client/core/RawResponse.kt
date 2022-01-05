package co.tala.example.http.client.core

import com.google.gson.Gson
import okhttp3.Response
import java.time.Instant

data class RawResponse(
    val gson: Gson,
    val okHttpResponse: Response,
    val start: Instant,
    val end: Instant
)
