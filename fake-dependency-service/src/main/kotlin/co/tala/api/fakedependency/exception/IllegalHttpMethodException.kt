package co.tala.api.fakedependency.exception

import co.tala.api.fakedependency.constant.HttpMethod

class IllegalHttpMethodException(httpMethod: String) : Exception(
    "$httpMethod is not a valid Http Method! Valid Http Methods are ${
        HttpMethod.values().filterNot { it == HttpMethod.NONE }.joinToString(",")
    }."
)
