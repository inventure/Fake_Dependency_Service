package co.tala.api.fakedependency.business.composer

import co.tala.api.fakedependency.business.helper.IKeyHelper
import co.tala.api.fakedependency.business.parser.IPayloadParser
import co.tala.api.fakedependency.business.parser.IQueryParser
import co.tala.api.fakedependency.redis.IRedisService
import co.tala.api.fakedependency.redis.RedisKeyPrefix
import co.tala.api.fakedependency.redis.RedisOpsType
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
        val requestQuery = queryParser.getQuery(request)
        val redisKeyWithQuery = makeKeyWithQuery(redisKey, requestQuery)
        val mockExists = redisSvc.hasKey(RedisKeyPrefix.EXECUTE, RedisOpsType.VALUE, redisKeyWithQuery)
        // Check URI values first and return that redis key if mock exists
        if (mockExists)
            redisKeyWithQuery
        else {
            // If mock does not exist with given URI, then parse the request payload for key values
            val queryKeys: Set<String> = redisSvc.getSetValues(
                keyPrefix = RedisKeyPrefix.QUERY,
                key = redisKey,
                type = object : TypeReference<Set<String>>() {}
            )
            val parsedQuery = queryKeys.mapNotNull { key: String ->
                val result = if (payload != null) parser.parse(payload, key) else null
                if (result != null) key to listOf(result) else null
            }.toMap()
            makeKeyWithQuery(redisKey, parsedQuery)
        }
    }

    private fun makeKeyWithQuery(redisKey: String, query: Map<String, List<String>>): String {
        val keys = query.keys
        val values = query.values
        return keyHelper.concatenateKeys(redisKey, *keys.plus(values).toTypedArray())
    }

}
