package co.tala.api.fakedependency.business.composer

import co.tala.api.fakedependency.business.helper.IKeyHelper
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

@Component
class RedisKeyComposer(
    private val keyHelper: IKeyHelper
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
    ): List<String> = listOfNotNull(
        getKey(requestId, request),
        if (includeWithoutRequestId)
            getKey(null, request)
        else null
    ).distinct()

    /**
     * Concatenates the requestId to the request uri
     */
    private fun getKey(requestId: String?, request: HttpServletRequest): String {
        val uri = request.requestURI.replaceFirst(Regex("/(mock-resources)"), "")
        val keys = listOfNotNull(requestId, uri).toTypedArray()
        return keyHelper.concatenateKeys(*keys)
    }
}
