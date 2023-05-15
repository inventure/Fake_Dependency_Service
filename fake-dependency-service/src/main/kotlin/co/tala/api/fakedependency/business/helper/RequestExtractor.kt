package co.tala.api.fakedependency.business.helper

import co.tala.api.fakedependency.configuration.helper.RequestIdExtractorConfiguration
import co.tala.api.fakedependency.constant.Constant.MOCK_RESOURCES
import co.tala.api.fakedependency.constant.HttpMethod
import co.tala.api.fakedependency.extension.getHttpMethod
import co.tala.api.fakedependency.extension.hasMockResources
import co.tala.api.fakedependency.redis.IRedisService
import co.tala.api.fakedependency.redis.RedisKeyPrefix
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

@Component
class RequestExtractor(
    private val keyHelper: IKeyHelper,
    private val config: RequestIdExtractorConfiguration,
    private val redisSvc: IRedisService,
    private val objectMapper: ObjectMapper
) : IRequestExtractor {
    companion object {
        private const val X_FAKE_DEPENDENCY_PARSE_PAYLOAD_HEADER: String = "X-Fake-Dependency-Parse-Payload-Header"
        private const val X_FAKE_DEPENDENCY_HTTP_METHOD: String = "X-Fake-Dependency-Http-Method"
    }

    override fun getRequestId(request: HttpServletRequest): String? = config.headers
        .split(",")
        .mapNotNull { request.getHeader(it) }
        .let {
            if (it.isEmpty())
                null
            else
                keyHelper.concatenateKeys(*it.toTypedArray())
        }

    override fun getRequestHeaders(request: HttpServletRequest): Map<String, List<String>> =
        request.headerNames.toList().associateWith {
            request.getHeaders(it).toList()
        }

    override fun setPayloadRequestHeaderName(redisKey: String, request: HttpServletRequest) {
        val header = request.getHeader(X_FAKE_DEPENDENCY_PARSE_PAYLOAD_HEADER)
        if (header != null)
            redisSvc.setValue(RedisKeyPrefix.PARSE_PAYLOAD_REQUEST_HEADER, redisKey, header)
    }

    override fun getPayloadFromRequestHeaders(redisKey: String, request: HttpServletRequest): Any? = redisSvc.getValue(
        RedisKeyPrefix.PARSE_PAYLOAD_REQUEST_HEADER,
        redisKey,
        object : TypeReference<String>() {}
    )?.let {
        val payload = request.getHeader(it)

        try {
            // If JSON, convert to MAP
            objectMapper.readValue(payload, object : TypeReference<Map<String, Any>>() {})
        } catch (e: Exception) {
            // Else return the RAW STRING (i.e XML)
            payload
        }
    }

    override fun getHttpMethod(request: HttpServletRequest): HttpMethod = when {
        // If the endpoint is for setup or verify, use the Request Header
        request.hasMockResources() -> request
            .getHeader(X_FAKE_DEPENDENCY_HTTP_METHOD)
            ?.let { HttpMethod.of(it.uppercase().trim()) } ?: HttpMethod.NONE
        // The if endpoint is the execution of mock, use the actual HTTP Method
        else -> request.getHttpMethod()
    }
}
