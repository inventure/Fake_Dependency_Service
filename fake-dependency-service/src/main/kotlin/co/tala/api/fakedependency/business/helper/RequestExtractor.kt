package co.tala.api.fakedependency.business.helper

import co.tala.api.fakedependency.configuration.helper.RequestIdExtractorConfiguration
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import javax.servlet.http.HttpServletRequest

@Component
class RequestExtractor(
    private val keyHelper: IKeyHelper,
    private val config: RequestIdExtractorConfiguration
) : IRequestExtractor {
    override fun getRequestId(request: HttpServletRequest): String? = config.headers
        .split(",")
        .mapNotNull { request.getHeader(it) }
        .let {
            if (it.isEmpty())
                null
            else
                keyHelper.concatenateKeys(*it.toTypedArray())
        }

    override fun getRequestHeaders(request: HttpServletRequest): Map<String, List<String>> =
        request.headerNames.toList().associateWith {
            request.getHeaders(it).toList()
        }
}
