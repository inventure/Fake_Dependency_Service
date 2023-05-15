package co.tala.api.fakedependency.constant

import co.tala.api.fakedependency.exception.IllegalHttpMethodException

enum class HttpMethod (val value: String?) {
    POST("post"),
    PUT("put"),
    PATCH("patch"),
    GET("get"),
    DELETE("delete"),
    OPTIONS("options"),
    HEAD("head"),
    TRACE("trace"),
    CONNECT("connect"),
    NONE(null);

    companion object {
        fun of(value: String): HttpMethod = try {
            valueOf(value.uppercase().trim())
        } catch (e: IllegalArgumentException) {
            throw IllegalHttpMethodException(value)
        }
    }
}
