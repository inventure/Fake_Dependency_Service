package co.tala.api.fakedependency.configuration.redis

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

@Configuration
@Component
@ConfigurationProperties(prefix = "config.redis")
data class RedisConfigProperties(
    var ttl: Long = 1800,
    var hostname: String = "",
    var port: Int = 0
)
