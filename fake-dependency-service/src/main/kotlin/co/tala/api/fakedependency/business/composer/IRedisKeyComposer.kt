package co.tala.api.fakedependency.business.composer

import javax.servlet.http.HttpServletRequest

interface IRedisKeyComposer {
    fun getKeys(requestId: String?, request: HttpServletRequest, includeWithoutRequestId: Boolean): List<String>
}
