package co.tala.api.fakedependency.business.mockdata.defaultmock

import co.tala.api.fakedependency.model.MockData
import co.tala.api.fakedependency.model.ResponseSetUpMetadata

/**
 * An implementation of a [Defaultable] can be used to return default mock data when there is no setup involved.
 * It can also optionally invoke an async callback afterwards, which can be used to do anything, but typically to
 * call back another service. For both the [getMockData] and [getCallback] methods, the request payload of the mock
 * is accessible. This is so that the business logic can extract properties from the payload. For example, if a userId
 * is exists in the payload and needs to be returned or forwarded to another service, it can be extracted from the
 * payload.
 */
interface Defaultable {
    fun Defaultable.mockData(responseBody: Any, httpStatus: Int = 200): MockData = MockData(
        responseBody = responseBody,
        responseSetUpMetadata = ResponseSetUpMetadata(
            httpStatus = httpStatus,
            delayMs = 0
        )
    )

    fun getMockData(payload: Any?): MockData
    fun getUriRegex(): Regex
    fun getCallback(): DefaultCallback = DefaultCallback()
}
