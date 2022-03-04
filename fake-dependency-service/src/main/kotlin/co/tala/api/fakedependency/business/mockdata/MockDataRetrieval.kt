package co.tala.api.fakedependency.business.mockdata

import co.tala.api.fakedependency.model.MockData

data class MockDataRetrieval(
    // The redis key to the mock data. Would be null in the case of default mock.
    val redisKey: String?,
    val mockData: MockData
)
