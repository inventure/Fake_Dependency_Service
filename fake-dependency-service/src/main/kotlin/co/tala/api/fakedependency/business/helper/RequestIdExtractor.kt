package co.tala.api.fakedependency.business.helper

import co.tala.api.fakedependency.configuration.helper.RequestIdExtractorConfiguration
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

@Component
class RequestIdExtractor(private val config: RequestIdExtractorConfiguration) : IRequestIdExtractor {
    override fun getRequestId(request: HttpServletRequest): String? = request.getHeader(config.header)
}
