package co.tala.api.fakedependency.business.composer

import javax.servlet.http.HttpServletRequest

interface IHKeyWithQueryComposer {
    fun getKeys(
        requestId: String?,
        request: HttpServletRequest,
        payload: Any?
    ): List<String>
}
