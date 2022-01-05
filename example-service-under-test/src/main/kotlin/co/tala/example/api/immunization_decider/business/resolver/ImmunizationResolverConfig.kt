package co.tala.example.api.immunization_decider.business.resolver

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ImmunizationResolverConfig {
    @Bean
    fun resolvers(): List<IImmunizationTypeResolver> = listOf(
        Covid19ImmunizationTypeResolver(),
        TdapImmunizationTypeResolver(),
        InfluenzaImmunizationTypeResolver()
    )
}
