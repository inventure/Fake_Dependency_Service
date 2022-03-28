package co.tala.api.fakedependency.business.parser

import co.tala.api.fakedependency.constant.VerifyMockContent
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

@Component
class QueryParser : IQueryParser {
    override fun getQuery(request: HttpServletRequest): Map<String, List<String>> = request.queryString
        ?.split('&')
        ?.groupBy {
            it.split('=').first()
        }?.filterNot {
            // Exclude the verification query params
            it.key == VerifyMockContent.QUERY_PARAM_KEY
        }?.map { entry ->
            entry.key to entry.value.map { value -> value.split('=').last() }
        }?.toMap() ?: emptyMap()
}
