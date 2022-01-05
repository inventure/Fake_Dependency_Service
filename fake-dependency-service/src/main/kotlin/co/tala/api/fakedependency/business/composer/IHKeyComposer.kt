package co.tala.api.fakedependency.business.composer

import javax.servlet.http.HttpServletRequest

interface IHKeyComposer {
    fun getKeys(requestId: String?, request: HttpServletRequest): List<String>
}
