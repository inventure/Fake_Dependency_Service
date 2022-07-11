package co.tala.api.fakedependency.configuration.helper

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.CommonsRequestLoggingFilter


@Configuration
class RequestLoggingFilterConfig {

    /**
     * For local testing. Logs incoming requests to this service,
     * which can help identify mocks that were not set up.
     *
     * @see: https://www.baeldung.com/spring-http-logging
     */
    @Bean
    fun logFilter(): CommonsRequestLoggingFilter =
        CommonsRequestLoggingFilter().also { filter ->
            filter.setIncludeQueryString(true)
            filter.setIncludePayload(true)
            filter.setMaxPayloadLength(10000)
            filter.setIncludeHeaders(true)
            filter.setAfterMessagePrefix("Incoming Request: ")
        }

}
