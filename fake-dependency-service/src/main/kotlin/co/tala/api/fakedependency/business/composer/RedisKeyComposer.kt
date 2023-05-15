package co.tala.api.fakedependency.business.composer

import co.tala.api.fakedependency.business.helper.IKeyHelper
import co.tala.api.fakedependency.business.helper.IRequestExtractor
import co.tala.api.fakedependency.constant.Constant.MOCK_RESOURCES
import co.tala.api.fakedependency.constant.HttpMethod
import co.tala.api.fakedependency.extension.getHttpMethod
import co.tala.api.fakedependency.extension.hasMockResources
import co.tala.api.fakedependency.redis.IRedisService
import co.tala.api.fakedependency.redis.RedisKeyPrefix
import com.fasterxml.jackson.core.type.TypeReference
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

@Component
class RedisKeyComposer(
    private val keyHelper: IKeyHelper,
    private val requestExtractor: IRequestExtractor,
    private val redisService: IRedisService
) : IRedisKeyComposer {
    /**
     * Gets hash keys both with and without requestId
     *
     * For PERFORMANCE REASONS, include only with requestId if the mock setup is providing it.
     * SETTING UP WITHOUT REQUEST ID CAN HAVE PERFORMANCE IMPLICATIONS ON NOT UNIQUE URIS.
     * [includeWithoutRequestId] should be false on setups and true on executes and verifies.
     * If [includeWithoutRequestId] is false and the requestId is provided, then the mock will be setup
     * only with the requestId.
     * If [includeWithoutRequestId] is false and the requestId is null, then the mock will be setup without the
     * requestId.
     * If [includeWithoutRequestId] is true, then the keys returned will include both with and without requestId
     */
    override fun getKeys(
        requestId: String?,
        request: HttpServletRequest,
        includeWithoutRequestId: Boolean
    ): List<String> = requestExtractor.getHttpMethod(request).let { httpMethod ->
        listOfNotNull(
            getKey(requestId, httpMethod, request),
            if (includeWithoutRequestId)
                getKey(null, httpMethod, request)
            else null
        )
    }.distinct()

    /**
     * Concatenates the requestId and http method to the request uri
     */
    private fun getKey(requestId: String?, httpMethod: HttpMethod, request: HttpServletRequest): String {
        val uri = request.requestURI.replaceFirst(Regex("($MOCK_RESOURCES)"), "")
        val keyWithHttpMethod = listOfNotNull(
            requestId,
            httpMethod.value?.let { "/$it" },
            uri
        ).toTypedArray().let { keyHelper.concatenateKeys(*it) }
        val keyWithoutHttpMethod = listOfNotNull(
            requestId,
            uri
        ).toTypedArray().let { keyHelper.concatenateKeys(*it) }
        return when (request.hasMockResources()) {
            // Setup will push to set
            true -> keyWithHttpMethod.also {
                if (request.getHttpMethod() == HttpMethod.POST && httpMethod != HttpMethod.NONE)
                    redisService.pushSetValues(RedisKeyPrefix.HTTP_METHOD, keyWithoutHttpMethod, httpMethod)
            }

            // Execute will check if the method exists in the set.
            // If true, use in key. Else, do not use in key
            false -> redisService.getSetValues(
                keyPrefix = RedisKeyPrefix.HTTP_METHOD,
                key = keyWithoutHttpMethod,
                type = object : TypeReference<Set<HttpMethod>>() {}
            ).contains(httpMethod).let { isHttpMethodInSet ->
                when (isHttpMethodInSet) {
                    true -> keyWithHttpMethod
                    false -> keyWithoutHttpMethod
                }
            }
        }
    }
}
