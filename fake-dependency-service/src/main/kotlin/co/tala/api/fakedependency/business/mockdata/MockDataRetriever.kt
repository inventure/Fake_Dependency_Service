package co.tala.api.fakedependency.business.mockdata

import co.tala.api.fakedependency.business.composer.IHKeyWithQueryComposer
import co.tala.api.fakedependency.business.helper.ISleep
import co.tala.api.fakedependency.business.mockdata.defaultmock.IDefaultMockDataRetriever
import co.tala.api.fakedependency.model.MockData
import co.tala.api.fakedependency.redis.IRedisService
import co.tala.api.fakedependency.redis.RedisKey
import com.fasterxml.jackson.core.type.TypeReference
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

@Component
class MockDataRetriever(
    private val redisSvc: IRedisService,
    private val hKeyWithQueryComposer: IHKeyWithQueryComposer,
    private val defaultMockDataRetriever: IDefaultMockDataRetriever,
    private val sleep: ISleep
) : IMockDataRetriever {
    @Suppress("UNCHECKED_CAST")
    override fun getMockData(
        requestId: String?,
        request: HttpServletRequest,
        payload: Any?
    ): MockData = hKeyWithQueryComposer
        .getKeys(requestId, request, payload)
        .mapNotNull { hKey ->
            val mock: MockData? =
                redisSvc.getValue(RedisKey.EXECUTE_KEY, hKey, object : TypeReference<MockData>() {})
            val binary: ByteArray? =
                redisSvc.getValue(RedisKey.BINARY_KEY, hKey, object : TypeReference<ByteArray>() {})
            when {
                mock != null && binary != null -> MockData(
                    responseBody = binary,
                    responseSetUpMetadata = mock.responseSetUpMetadata
                )
                else -> mock
            }
        }.ifEmpty { null }.let { mocks ->
            when (mocks) {
                null -> defaultMockDataRetriever.getDefaultMockData(request, payload)
                else -> mocks.last().also {
                    sleep.forMillis(it.responseSetUpMetadata.delayMs)
                }
            }
        }
}
