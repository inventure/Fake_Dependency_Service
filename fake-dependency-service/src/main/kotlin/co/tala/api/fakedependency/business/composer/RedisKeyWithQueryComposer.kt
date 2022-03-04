package co.tala.api.fakedependency.business.composer

import co.tala.api.fakedependency.business.helper.IKeyHelper
import co.tala.api.fakedependency.business.parser.IPayloadParser
import co.tala.api.fakedependency.business.parser.IQueryParser
import co.tala.api.fakedependency.redis.IRedisService
import co.tala.api.fakedependency.redis.RedisKeyPrefix
import com.fasterxml.jackson.core.type.TypeReference
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

@Component
class RedisKeyWithQueryComposer(
    private val redisSvc: IRedisService,
    private val redisKeyComposer: IRedisKeyComposer,
    private val queryParser: IQueryParser,
    private val keyHelper: IKeyHelper,
    private val parser: IPayloadParser
) : IRedisKeyWithQueryComposer {
    override fun getKeys(
        requestId: String?,
        request: HttpServletRequest,
        payload: Any?
    ): List<String> = redisKeyComposer.getKeys(
        requestId = requestId,
        request = request,
        includeWithoutRequestId = true
    ).map { redisKey ->
        val querySet = redisSvc.getSetValues(
            keyPrefix = RedisKeyPrefix.QUERY,
            key = redisKey,
            type = object : TypeReference<Set<Map<String, String>>>() {}
        )

        // Check URI values first
        val requestQuery = queryParser.getQuery(request)
        val matchingQuery: Map<String, String> = querySet.firstOrNull { it == requestQuery } ?: emptyMap()

        matchingQuery.ifEmpty {
            // If URI does not have query params, then check the request payload
            querySet.flatMap { it.keys }.distinct().mapNotNull { key: String ->
                val result = if (payload != null) parser.parse(payload, key) else null
                if (result != null) key to result else null
            }.toMap()
        }.let {
            keyHelper.concatenateKeys(redisKey, *it.keys.plus(it.values).toTypedArray())
        }
    }

}
