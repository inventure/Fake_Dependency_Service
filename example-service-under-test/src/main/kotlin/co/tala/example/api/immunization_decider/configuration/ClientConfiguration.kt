package co.tala.example.api.immunization_decider.configuration

import co.tala.example.http.client.lib.factory.ClientFactory
import co.tala.example.http.client.lib.factory.IClientFactory
import co.tala.example.http.client.lib.service.immunization_history.IImmunizationHistoryClient
import co.tala.example.http.client.lib.service.pharmacy.IPharmacyClient
import co.tala.example.http.client.lib.service.user.IUserClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ClientConfiguration(
    private val config: ClientConfigurationProperties
) {
    @Bean
    fun userClientFactory(): IClientFactory<IUserClient> = ClientFactory(
        clazz = IUserClient::class.java,
        baseUrl = config.user
    )

    @Bean
    fun immunizationHistoryClientFactory(): IClientFactory<IImmunizationHistoryClient> = ClientFactory(
        clazz = IImmunizationHistoryClient::class.java,
        baseUrl = config.immunizationhistory
    )

    @Bean
    fun pharmacyClientFactory(): IClientFactory<IPharmacyClient> = ClientFactory(
        clazz = IPharmacyClient::class.java,
        baseUrl = config.pharmacy
    )
}
