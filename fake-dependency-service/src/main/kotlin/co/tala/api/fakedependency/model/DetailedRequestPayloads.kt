package co.tala.api.fakedependency.model


data class DetailedRequestPayloads(val count: Int, val requests: List<RequestPayload>) {
    data class RequestPayload(
        val payload: Any? = null,
        val headers: Map<String, List<String>>? = null
    )
}
