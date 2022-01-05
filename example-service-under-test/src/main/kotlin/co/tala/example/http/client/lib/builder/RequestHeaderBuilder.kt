package co.tala.example.http.client.lib.builder

import co.tala.example.http.client.lib.auth.IAuthenticator

interface IRequestHeaderBuilder {
    fun addHeader(key: String, value: String?): IRequestHeaderBuilder
    fun clear(): IRequestHeaderBuilder
    fun build(): Map<String, String>
}

class RequestHeaderBuilder(
    private val authenticator: IAuthenticator? = null,
    private val requestId: String? = null
) : IRequestHeaderBuilder {
    companion object {
        private const val X_REQUEST_ID: String = "X-Request-ID"
    }

    private val headers = mutableMapOf<String, String>()

    override fun addHeader(key: String, value: String?): IRequestHeaderBuilder = apply {
        if (value != null)
            headers[key] = value
    }

    override fun clear(): IRequestHeaderBuilder = RequestHeaderBuilder(authenticator, requestId)

    override fun build(): Map<String, String> = addAuthorization().addRequestId().headers.toMap()

    private fun addAuthorization(): RequestHeaderBuilder = apply {
        if (authenticator != null) {
            val token = authenticator.getToken()
            headers[token.authorizationType] = "${token.tokenType} ${token.tokenValue}".trim()
        }
    }

    private fun addRequestId(): RequestHeaderBuilder = apply {
        if (requestId != null)
            headers[X_REQUEST_ID] = requestId
    }
}
