package co.tala.api.fakedependency.configuration.redis

import io.lettuce.core.ClientOptions
import io.lettuce.core.resource.ClientResources
import io.lettuce.core.resource.DefaultClientResources
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration
import org.springframework.data.redis.core.RedisOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.session.data.redis.config.ConfigureRedisAction

@Configuration
class RedisConnectionConfiguration(
    private val config: RedisConfigProperties
) {
    @Bean
    fun configureRedisAction(): ConfigureRedisAction? = ConfigureRedisAction.NO_OP

    @Bean(destroyMethod = "shutdown")
    fun clientResources(): ClientResources = DefaultClientResources.create()

    @Bean
    fun clientOptions(): ClientOptions = ClientOptions.builder()
        .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
        .autoReconnect(true)
        .build()

    @Bean
    fun lettucePoolConfig(options: ClientOptions, dcr: ClientResources): LettucePoolingClientConfiguration =
        LettucePoolingClientConfiguration.builder()
            .poolConfig(GenericObjectPoolConfig<Any>())
            .clientOptions(options)
            .clientResources(dcr)
            .build()

    @Bean
    fun redisStandaloneConfiguration(): RedisStandaloneConfiguration =
        RedisStandaloneConfiguration(config.hostname, config.port)

    @Bean
    fun redisConnectionFactory(
        redisStandaloneConfiguration: RedisStandaloneConfiguration,
        lettucePoolConfig: LettucePoolingClientConfiguration
    ): RedisConnectionFactory = LettuceConnectionFactory(redisStandaloneConfiguration, lettucePoolConfig)

    @Bean
    fun redisOperations(redisConnectionFactory: RedisConnectionFactory): RedisOperations<Any, Any> {
        val template = RedisTemplate<Any, Any>()
        template.setConnectionFactory(redisConnectionFactory)
        template.setDefaultSerializer(StringRedisSerializer())
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = GenericJackson2JsonRedisSerializer()
        template.hashKeySerializer = StringRedisSerializer()
        template.hashValueSerializer = GenericJackson2JsonRedisSerializer()
        template.afterPropertiesSet()
        return template
    }

}
