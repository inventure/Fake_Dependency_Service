package co.tala.api.fakedependency.business.mockdata

import co.tala.api.fakedependency.business.helper.ISleep
import co.tala.api.fakedependency.business.mockdata.defaultmock.IDefaultMockDataRetriever
import co.tala.api.fakedependency.model.MockData
import co.tala.api.fakedependency.redis.IRedisService
import co.tala.api.fakedependency.redis.RedisKeyPrefix
import com.fasterxml.jackson.core.type.TypeReference
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

@Component
class MockDataRetriever(
    private val redisSvc: IRedisService,
    private val defaultMockDataRetriever: IDefaultMockDataRetriever,
    private val sleep: ISleep
) : IMockDataRetriever {
    @Suppress("UNCHECKED_CAST")
    override fun getMockData(redisKeys: List<String>, request: HttpServletRequest, payload: Any?): MockDataRetrieval =
        redisKeys
            .firstNotNullOfOrNull { redisKey ->
                val mock: MockData? =
                    redisSvc.popListValue(RedisKeyPrefix.EXECUTE, redisKey, object : TypeReference<MockData>() {})
                val binary: ByteArray? =
                    redisSvc.popListValue(RedisKeyPrefix.BINARY, redisKey, object : TypeReference<ByteArray>() {})
                when {
                    mock != null && binary != null -> MockData(
                        responseBody = binary,
                        responseSetUpMetadata = mock.responseSetUpMetadata,
                        responseHeaders = mock.responseHeaders
                    )
                    else -> mock
                }.let {
                    if (it != null) {
                        sleep.forMillis(it.responseSetUpMetadata.delayMs)
                        MockDataRetrieval(
                            redisKey = redisKey,
                            mockData = it
                        )
                    } else null
                }
            } ?: MockDataRetrieval(
            // If the mock is not found, then try getting default mock data.
            // default data is not stored in Redis.
            // as a result, we won't be able to hit verify endpoint for default mocks.
            redisKey = null,
            mockData = defaultMockDataRetriever.getDefaultMockData(request, payload)
        )

}
