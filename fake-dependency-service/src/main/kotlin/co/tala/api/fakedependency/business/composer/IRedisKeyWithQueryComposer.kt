package co.tala.api.fakedependency.business.composer

import javax.servlet.http.HttpServletRequest

interface IRedisKeyWithQueryComposer {
    fun getKeys(
        requestId: String?,
        request: HttpServletRequest,
        payload: Any?
    ): List<String>
}
