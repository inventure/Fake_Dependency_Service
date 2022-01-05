package co.tala.api.fakedependency.configuration.defaultmock

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

@Configuration
@Component
@ConfigurationProperties(prefix = "config.default.callbacks")
data class DefaultCallbackConfiguration(
        var minimumDelay: Long = 0,
        var maximumDelay: Long = 0,
        var enabled: Boolean = false
)
