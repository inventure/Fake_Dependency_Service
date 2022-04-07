package co.tala.example.http.client.core.request

import co.tala.example.http.client.core.ContentType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

interface BinaryBodyRequest {
    fun toRequestBody(): RequestBody = binary().toRequestBody(contentType().value.toMediaType())
    fun BinaryBodyRequest.contentType(): ContentType
    fun BinaryBodyRequest.binary(): ByteArray
}

@Suppress("ArrayInDataClass")
data class OffsetOctetStreamBodyRequest(private val binary: ByteArray) : BinaryBodyRequest {
    override fun BinaryBodyRequest.contentType(): ContentType = ContentType.APPLICATION_OFFSET_OCTET_STREAM
    override fun BinaryBodyRequest.binary(): ByteArray = binary
}

@Suppress("ArrayInDataClass")
data class OctetStreamBodyRequest(private val binary: ByteArray) : BinaryBodyRequest {
    override fun BinaryBodyRequest.contentType(): ContentType = ContentType.APPLICATION_OCTET_STREAM
    override fun BinaryBodyRequest.binary(): ByteArray = binary
}
