package co.tala.api.fakedependency.business.mockdata

import co.tala.api.fakedependency.model.MockData
import javax.servlet.http.HttpServletRequest

interface IMockDataRetriever {
    fun getMockData(
        requestId: String?,
        request: HttpServletRequest,
        payload: Any?
    ): MockData
}
