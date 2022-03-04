package co.tala.api.fakedependency.business.mockdata

import javax.servlet.http.HttpServletRequest

interface IMockDataRetriever {
    fun getMockData(
        redisKeys: List<String>,
        request: HttpServletRequest,
        payload: Any?
    ): MockDataRetrieval
}
