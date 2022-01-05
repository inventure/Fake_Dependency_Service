package co.tala.api.fakedependency.business.parser

import javax.servlet.http.HttpServletRequest

interface IQueryParser {
    fun getQuery(request: HttpServletRequest): Map<String, String>
}
