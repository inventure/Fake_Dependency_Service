package co.tala.example.http.client.core.request

import co.tala.example.http.client.core.ContentType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface MultipartBodyRequest {
    fun toRequestBody(): MultipartBody
    fun MultipartBodyRequest.contentType(): ContentType
    fun MultipartBodyRequest.build(block: MultipartBody.Builder.() -> MultipartBody.Builder) =
        MultipartBody.Builder().setType(contentType().value.toMediaType()).let { block(it).build() }
}

interface MultipartFormDataBodyRequest : MultipartBodyRequest {
    override fun MultipartBodyRequest.contentType(): ContentType = ContentType.MULTIPART_FORM_DATA
}

fun MultipartBody.Builder.tryAddFormData(name: String, value: String?) = apply {
    if (value != null) addFormDataPart(name, value)
}

fun MultipartBody.Builder.tryAddFormData(name: String, fileName: String?, body: RequestBody?) = apply {
    if (body != null) addFormDataPart(name, fileName, body)
}

