package co.tala.api.fakedependency.configuration.defaultmock

import co.tala.api.fakedependency.business.mockdata.defaultmock.Defaultable
import co.tala.api.fakedependency.business.mockdata.defaultmock.example.ExampleDefault
import co.tala.api.fakedependency.redis.IRedisService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DefaultMockConfiguration(
    private val redisSvc: IRedisService
) {

    @Bean
    fun defaultables(): List<Defaultable> = listOf(
        ExampleDefault(redisSvc)
    )
}
