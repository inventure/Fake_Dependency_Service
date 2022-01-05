package co.tala.api.fakedependency.business.provider

import co.tala.api.fakedependency.business.composer.IHKeyComposer
import co.tala.api.fakedependency.business.composer.IHKeyWithQueryComposer
import co.tala.api.fakedependency.business.helper.IKeyHelper
import co.tala.api.fakedependency.business.helper.IRequestIdExtractor
import co.tala.api.fakedependency.business.mockdata.IMockDataRetriever
import co.tala.api.fakedependency.business.parser.IQueryParser
import co.tala.api.fakedependency.model.MockData
import co.tala.api.fakedependency.redis.IRedisService
import co.tala.api.fakedependency.redis.RedisKey.Companion.BINARY_KEY
import co.tala.api.fakedependency.redis.RedisKey.Companion.EXECUTE_KEY
import co.tala.api.fakedependency.redis.RedisKey.Companion.QUERY_KEY
import co.tala.api.fakedependency.redis.RedisKey.Companion.VERIFY_KEY
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

@Component
internal class FakeDependencyProvider(
    private val redisSvc: IRedisService,
    private val queryParser: IQueryParser,
    private val hKeyComposer: IHKeyComposer,
    private val hKeyWithQueryComposer: IHKeyWithQueryComposer,
    private val mockDataRetriever: IMockDataRetriever,
    private val keyHelper: IKeyHelper,
    private val requestIdExtractor: IRequestIdExtractor,
    private val objectMapper: ObjectMapper
) : IFakeDependencyProvider {

    override fun setup(
        mockData: MockData,
        request: HttpServletRequest
    ): ResponseEntity<MockData> {
        val requestId = requestIdExtractor.getRequestId(request)
        val query = queryParser.getQuery(request)
        hKeyComposer.getKeys(requestId, request).forEach { hKey ->
            if (query.isNotEmpty()) redisSvc.setValue(QUERY_KEY, hKey, query)
            val hKeyWithQuery = keyHelper.concatenateKeys(hKey, *query.values.toTypedArray())
            redisSvc.setValue(EXECUTE_KEY, hKeyWithQuery, mockData)
        }
        return ResponseEntity.ok(mockData)
    }

    override fun patchSetup(
        request: HttpServletRequest
    ): ResponseEntity<Unit> {
        val requestId = requestIdExtractor.getRequestId(request)
        val query = queryParser.getQuery(request)
        hKeyComposer.getKeys(requestId, request).forEach { hKey ->
            val hKeyWithQuery = keyHelper.concatenateKeys(hKey, *query.values.toTypedArray())
            val bytes = request.inputStream?.readAllBytes()
            if (bytes != null)
                redisSvc.setValue(BINARY_KEY, hKeyWithQuery, bytes)
        }
        return ResponseEntity.noContent().build()
    }

    override fun execute(
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val requestId = requestIdExtractor.getRequestId(request)
        // For GET and DELETE, the payload will be empty string. We should consider that null so that it's not parsed.
        val payload: Any? = if (listOf("GET", "DELETE").contains(request.method)) null else {
            val payload = String(request.inputStream.readAllBytes())
            try {
                // If JSON, convert to MAP
                objectMapper.readValue(payload, object : TypeReference<Map<String, Any>>() {})
            } catch (e: Exception) {
                // Else return the RAW STRING (i.e XML)
                payload
            }
        }
        return mockDataRetriever.getMockData(
            requestId = requestId,
            request = request,
            payload = payload
        ).toResponseEntity().also {
            hKeyWithQueryComposer.getKeys(requestId = requestId, request = request, payload = payload).forEach { hKey ->
                redisSvc.pushListValue(VERIFY_KEY, hKey, payload ?: "")
            }
        }
    }

    override fun verify(
        request: HttpServletRequest
    ): ResponseEntity<List<Any>> =
        ResponseEntity.ok(
            redisSvc.getListValues(
                key = VERIFY_KEY,
                hKey = hKeyWithQueryComposer.getKeys(
                    requestId = requestIdExtractor.getRequestId(request),
                    request = request,
                    payload = queryParser.getQuery(request)
                ).first(),
                type = object : TypeReference<List<Any>>() {}
            )
        )
}
