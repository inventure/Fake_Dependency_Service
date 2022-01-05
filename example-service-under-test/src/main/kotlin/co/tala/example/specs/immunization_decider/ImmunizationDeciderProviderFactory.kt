package co.tala.example.specs.immunization_decider

import co.tala.example.specs.core.lib.nextLong
import co.tala.example.specs.core.lib.randomUUID
import co.tala.example.http.client.lib.factory.createClient
import co.tala.example.http.client.lib.service.immunization_history.model.ImmunizationOccurrence

class ImmunizationDeciderProviderFactory(
    private val config: ImmunizationDeciderProviderFactoryConfig
) {
    fun create(bornDaysAgo: Long, immunizationOccurrences: List<ImmunizationOccurrence>): ImmunizationDeciderProvider {
        val requestId = randomUUID()
        val scenario = ImmunizationDeciderScenario(
            sourceRefId = randomUUID(),
            userId = nextLong(),
            bornDaysAgo = bornDaysAgo,
            requestId = requestId,
            immunizationOccurrences = immunizationOccurrences
        )
        val client = ImmunizationDeciderClientContainer(
            mockUser = createClient("${config.fakeDependencyApiUrl}/user-service/mock-resources", requestId = requestId),
            mockPharmacy = createClient("${config.fakeDependencyApiUrl}/pharmacy-service/mock-resources", requestId = requestId),
            mockImmunizationHistory = createClient("${config.fakeDependencyApiUrl}/immunization-history-service/mock-resources", requestId = requestId),
            immunizationDecider = createClient(config.immunizationDeciderApiUrl, requestId = requestId)
        )
        return ImmunizationDeciderProvider(scenario, client)
    }
}
