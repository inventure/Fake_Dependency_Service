package co.tala.api.fakedependency.redis

class RedisKey {
    companion object {
        const val EXECUTE_KEY = "fake-dependency/execute"
        const val QUERY_KEY = "fake-dependency/query"
        const val VERIFY_KEY = "fake-dependency/verify"
        const val BINARY_KEY = "fake-dependency/binary"
    }
}
