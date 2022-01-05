package co.tala.api.fakedependency.business.mockdata.defaultmock

import co.tala.api.fakedependency.model.MockData
import javax.servlet.http.HttpServletRequest

interface IDefaultMockDataRetriever {
    fun getDefaultMockData(request: HttpServletRequest, payload: Any?): MockData
}
