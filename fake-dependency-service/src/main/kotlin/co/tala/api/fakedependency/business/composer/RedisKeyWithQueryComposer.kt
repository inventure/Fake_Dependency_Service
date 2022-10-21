package co.tala.api.fakedependency.business.composer

import co.tala.api.fakedependency.business.helper.IKeyHelper
import co.tala.api.fakedependency.business.helper.IRequestExtractor
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
    private val parser: IPayloadParser,
    private val requestExtractor: IRequestExtractor
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
            val queryKeys: Set<String> = redisSvc.getSetValues(
                keyPrefix = RedisKeyPrefix.QUERY,
                key = redisKey,
                type = object : TypeReference<Set<String>>() {}
            )
            // override payload to parse a request header if the mock was set up with X-Fake-Dependency-Parse-Payload-Header header
            val payloadOverride: Any? = requestExtractor.getPayloadFromRequestHeaders(redisKey, request)
            // If mock does not exist with given URI, then parse the request payload for key values
            val parsedQuery = queryKeys.sorted().mapNotNull { key: String ->
                val result = when {
                    payloadOverride != null -> parser.parse(payloadOverride, key)
                    payload != null -> parser.parse(payload, key)
                    else -> null
                }
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
