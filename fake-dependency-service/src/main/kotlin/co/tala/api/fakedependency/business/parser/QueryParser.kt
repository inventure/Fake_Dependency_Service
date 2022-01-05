package co.tala.api.fakedependency.business.parser

import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

@Component
class QueryParser : IQueryParser {
    override fun getQuery(request: HttpServletRequest): Map<String, String> = request.queryString
        ?.split('&')
        ?.associate {
            val result = it.split('=')
            result.first() to result.last()
        } ?: emptyMap()
}
