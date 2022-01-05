package co.tala.api.fakedependency.business.helper

import javax.servlet.http.HttpServletRequest

interface IRequestIdExtractor {
    fun getRequestId(request: HttpServletRequest) : String?
}
