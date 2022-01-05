package co.tala.example.api.immunization_decider.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

@Configuration
@Component
@ConfigurationProperties(prefix = "config.client.baseurl")
data class ClientConfigurationProperties(
    var user: String = "",
    var pharmacy: String = "",
    var immunizationhistory: String = ""
)
