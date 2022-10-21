package co.tala.api.fakedependency.business.helper

import javax.servlet.http.HttpServletRequest

interface IRequestExtractor {
    fun getRequestId(request: HttpServletRequest) : String?
    fun getRequestHeaders(request: HttpServletRequest): Map<String, List<String>>
    fun setPayloadRequestHeaderName(redisKey: String, request: HttpServletRequest)
    fun getPayloadFromRequestHeaders(redisKey: String, request: HttpServletRequest): Any?
}
