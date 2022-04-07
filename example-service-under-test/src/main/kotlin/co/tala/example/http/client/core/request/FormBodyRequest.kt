package co.tala.example.http.client.core.request

import okhttp3.FormBody
import okhttp3.MediaType

interface FormBodyRequest {
    fun toRequestBody(): FormBody
    fun FormBodyRequest.build(block: FormBody.Builder.() -> FormBody.Builder) =
        FormBody.Builder().let { block(it).build() }
}

fun FormBody.Builder.tryAddFormData(name: String, value: String?) = apply {
    if (value != null) add(name, value)
}
