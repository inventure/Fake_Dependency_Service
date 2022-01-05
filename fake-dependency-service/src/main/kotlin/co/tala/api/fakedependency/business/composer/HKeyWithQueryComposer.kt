package co.tala.api.fakedependency.business.composer

import co.tala.api.fakedependency.business.helper.IKeyHelper
import co.tala.api.fakedependency.business.parser.IPayloadParser
import co.tala.api.fakedependency.business.parser.IQueryParser
import co.tala.api.fakedependency.redis.IRedisService
import co.tala.api.fakedependency.redis.RedisKey.Companion.QUERY_KEY
import com.fasterxml.jackson.core.type.TypeReference
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

@Component
class HKeyWithQueryComposer(
    private val redisSvc: IRedisService,
    private val hKeyComposer: IHKeyComposer,
    private val queryParser: IQueryParser,
    private val keyHelper: IKeyHelper,
    private val parser: IPayloadParser
) : IHKeyWithQueryComposer {
    override fun getKeys(
        requestId: String?,
        request: HttpServletRequest,
        payload: Any?
    ): List<String> = hKeyComposer.getKeys(requestId, request).map { hKey ->
        // Check if we need to get values from payload
        val query: Map<String, String> = redisSvc.getValue(
            key = QUERY_KEY,
            hKey = hKey,
            type = object : TypeReference<Map<String, String>>() {}
        ) ?: emptyMap()
        val values =
            // Check URI values first
            queryParser.getQuery(request).values.intersect(query.values.toList())
            // If URI does not have query params, then check the request payload
            .ifEmpty {
                query.keys.mapNotNull { key: String ->
                    if (payload != null) parser.parse(payload, key) else null
                }
            }
        keyHelper.concatenateKeys(hKey, *values.toTypedArray())
    }

}
