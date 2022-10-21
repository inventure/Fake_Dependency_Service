package co.tala.api.fakedependency.redis

class RedisKeyPrefix {
    companion object {
        const val EXECUTE = "fake-dependency/execute"
        const val QUERY = "fake-dependency/query"
        const val VERIFY_PAYLOAD = "fake-dependency/verify-payload"
        const val VERIFY_HEADERS = "fake-dependency/verify-headers"
        const val BINARY = "fake-dependency/binary"
        const val PARSE_PAYLOAD_REQUEST_HEADER = "fake-dependency/parse-payload-request-header"
    }
}
