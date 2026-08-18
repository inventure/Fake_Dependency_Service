package co.tala.api.fakedependency.business.mockdata.defaultmock.braze

import co.tala.api.fakedependency.business.mockdata.defaultmock.Defaultable
import co.tala.api.fakedependency.model.MockData

class BrazeDataDefault : Defaultable {
    override fun getMockData(payload: Any?): MockData = mockData(
        responseBody = mapOf(
            "message" to "success"
        ),
        httpStatus = 200,
        responseHeaders = emptyMap()
    )

    override fun getUriRegex(): Regex = Regex("/braze/api/v3/data.*")
}

class BrazeContentCardsSyncDefault : Defaultable {
    override fun getMockData(payload: Any?): MockData = mockData(
        responseBody = mapOf(
            "cards" to emptyList<Any>(),
            "full_sync" to true
        ),
        httpStatus = 200,
        responseHeaders = emptyMap()
    )

    override fun getUriRegex(): Regex = Regex("/braze/api/v3/content_cards/sync.*")
}
