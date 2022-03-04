package co.tala.api.fakedependency.configuration.helper

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

@Configuration
@Component
@ConfigurationProperties(prefix = "config.request.id")
data class RequestIdExtractorConfiguration(
    var headers: String = ""
)
