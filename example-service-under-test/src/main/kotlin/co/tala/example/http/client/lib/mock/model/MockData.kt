package co.tala.example.http.client.lib.mock.model

data class MockData<T>(
    val responseBody: T? = null,
    val responseSetUpMetadata: ResponseSetUpMetadata = ResponseSetUpMetadata(),
) {
    data class ResponseSetUpMetadata(
        val httpStatus: Int = 200,
        val delayMs: Long = 0
    )
}
