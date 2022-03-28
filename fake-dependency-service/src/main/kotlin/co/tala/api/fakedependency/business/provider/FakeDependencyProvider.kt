package co.tala.api.fakedependency.business.provider

import co.tala.api.fakedependency.business.composer.IRedisKeyComposer
import co.tala.api.fakedependency.business.composer.IRedisKeyWithQueryComposer
import co.tala.api.fakedependency.business.helper.IKeyHelper
import co.tala.api.fakedependency.business.helper.IRequestExtractor
import co.tala.api.fakedependency.business.mockdata.IMockDataRetriever
import co.tala.api.fakedependency.business.parser.IQueryParser
import co.tala.api.fakedependency.model.MockData
import co.tala.api.fakedependency.model.DetailedRequestPayloads
import co.tala.api.fakedependency.redis.IRedisService
import co.tala.api.fakedependency.redis.RedisKeyPrefix
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

@Component
internal class FakeDependencyProvider(
    private val redisSvc: IRedisService,
    private val queryParser: IQueryParser,
    private val redisKeyComposer: IRedisKeyComposer,
    private val redisKeyWithQueryComposer: IRedisKeyWithQueryComposer,
    private val mockDataRetriever: IMockDataRetriever,
    private val keyHelper: IKeyHelper,
    private val requestExtractor: IRequestExtractor,
    private val objectMapper: ObjectMapper
) : IFakeDependencyProvider {

    override fun setup(
        mockData: MockData,
        request: HttpServletRequest
    ): ResponseEntity<MockData> {
        val requestId = requestExtractor.getRequestId(request)
        val query = queryParser.getQuery(request)
        redisKeyComposer.getKeys(
            requestId = requestId,
            request = request,
            includeWithoutRequestId = false
        ).forEach { redisKey ->
            if (query.isNotEmpty()) {
                redisSvc.pushSetValue(RedisKeyPrefix.QUERY, redisKey, query)
            }
            val redisKeyWithQuery = keyHelper.concatenateKeys(redisKey, *query.keys.plus(query.values).toTypedArray())
            redisSvc.pushListValue(RedisKeyPrefix.EXECUTE, redisKeyWithQuery, mockData)
        }
        return ResponseEntity.ok(mockData)
    }

    override fun patchSetup(
        request: HttpServletRequest
    ): ResponseEntity<Unit> {
        val requestId = requestExtractor.getRequestId(request)
        val query = queryParser.getQuery(request)
        val bytes = request.inputStream?.readAllBytes()
        redisKeyComposer.getKeys(
            requestId = requestId,
            request = request,
            includeWithoutRequestId = false
        ).forEach { redisKey ->
            val redisKeyWithQuery = keyHelper.concatenateKeys(redisKey, *query.keys.plus(query.values).toTypedArray())
            redisSvc.pushListValue(RedisKeyPrefix.BINARY, redisKeyWithQuery, bytes)
        }
        return ResponseEntity.noContent().build()
    }

    override fun execute(
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val requestId = requestExtractor.getRequestId(request)
        val headers = requestExtractor.getRequestHeaders(request)
        val payloadBinary: ByteArray = request.inputStream.readAllBytes()
        // For GET and DELETE, the payload will be empty string. We should consider that null so that it's not parsed.
        val payload: Any? = if (listOf("GET", "DELETE").contains(request.method)) null else {
            val payload = String(payloadBinary)
            try {
                // If JSON, convert to MAP
                objectMapper.readValue(payload, object : TypeReference<Map<String, Any>>() {})
            } catch (e: Exception) {
                // Else return the RAW STRING (i.e XML)
                payload
            }
        }
        val redisKeys = redisKeyWithQueryComposer.getKeys(requestId, request, payload)

        return mockDataRetriever.getMockData(
            redisKeys = redisKeys,
            request = request,
            payload = payload
        ).let {
            val redisKey = it.redisKey
            if (redisKey != null) {
                // Store the string or hashmap for verify list
                redisSvc.pushListValue(RedisKeyPrefix.VERIFY_PAYLOAD, redisKey, payload ?: "")
                // Store the binary for verifying the last. This is to be compatible with files, images, zips, etc.
                // We will only return the last payload for these types.
                redisSvc.setValue(RedisKeyPrefix.VERIFY_PAYLOAD, redisKey, payloadBinary)
                // Store the headers for verify detailed
                redisSvc.pushListValue(RedisKeyPrefix.VERIFY_HEADERS, redisKey, headers)
            }
            it.mockData.toResponseEntity()
        }
    }

    override fun verifyDetailed(
        request: HttpServletRequest
    ): ResponseEntity<DetailedRequestPayloads> {
        val requestId = requestExtractor.getRequestId(request)
        val redisKey = redisKeyWithQueryComposer.getKeys(
            requestId = requestId,
            request = request,
            payload = null
        ).first()
        val payloadVerify = redisSvc.getListValues(
            keyPrefix = RedisKeyPrefix.VERIFY_PAYLOAD,
            key = redisKey,
            type = object : TypeReference<List<Any>>() {}
        )
        val headersVerify = redisSvc.getListValues(
            keyPrefix = RedisKeyPrefix.VERIFY_HEADERS,
            key = redisKey,
            type = object : TypeReference<List<Map<String, List<String>>>>() {}
        )
        val payloads = payloadVerify.mapIndexed { index, any ->
            DetailedRequestPayloads.RequestPayload(
                payload = any,
                headers = headersVerify.takeIfSafe(index)
            )
        }
        return ResponseEntity.ok(
            DetailedRequestPayloads(
                count = payloads.size,
                requests = payloads
            )
        )
    }

    override fun verifyLast(
        request: HttpServletRequest
    ): ResponseEntity<ByteArray> =
        ResponseEntity.ok(
            redisSvc.getValue(
                keyPrefix = RedisKeyPrefix.VERIFY_PAYLOAD,
                key = redisKeyWithQueryComposer.getKeys(
                    requestId = requestExtractor.getRequestId(request),
                    request = request,
                    payload = null
                ).first(),
                type = object : TypeReference<ByteArray>() {}
            ) ?: byteArrayOf()
        )

    override fun verifyList(
        request: HttpServletRequest
    ): ResponseEntity<List<Any>> =
        ResponseEntity.ok(
            redisSvc.getListValues(
                keyPrefix = RedisKeyPrefix.VERIFY_PAYLOAD,
                key = redisKeyWithQueryComposer.getKeys(
                    requestId = requestExtractor.getRequestId(request),
                    request = request,
                    payload = null
                ).first(),
                type = object : TypeReference<List<Any>>() {}
            )
        )

    /**
     * Take a value from a List if index passed exists in the list.
     */
    private fun <T> List<T>.takeIfSafe(index: Int): T? = takeIf { index in 0 until size }?.let { it[index] }
}
