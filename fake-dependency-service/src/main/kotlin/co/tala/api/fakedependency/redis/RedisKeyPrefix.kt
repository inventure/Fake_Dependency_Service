package co.tala.api.fakedependency.redis

object RedisKeyPrefix {
    const val EXECUTE = "fake-dependency/execute"
    const val QUERY = "fake-dependency/query"
    const val HTTP_METHOD = "fake-dependency/http-method"
    const val VERIFY_PAYLOAD = "fake-dependency/verify-payload"
    const val VERIFY_HEADERS = "fake-dependency/verify-headers"
    const val BINARY = "fake-dependency/binary"
    const val PARSE_PAYLOAD_REQUEST_HEADER = "fake-dependency/parse-payload-request-header"
}
