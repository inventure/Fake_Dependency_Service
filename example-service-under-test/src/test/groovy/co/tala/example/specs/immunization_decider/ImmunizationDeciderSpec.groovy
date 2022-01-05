package co.tala.example.specs.immunization_decider

import co.tala.example.specs.config.Configuration
import co.tala.example.http.client.lib.service.immunization_history.model.ImmunizationOccurrence
import co.tala.example.http.client.lib.service.immunization_history.model.ImmunizationType
import co.tala.example.specs.core.lib.InstantLibraryKt
import spock.lang.Shared
import spock.lang.Specification

import java.time.Instant

abstract class ImmunizationDeciderSpec extends Specification {
    @Shared
    protected ImmunizationDeciderProviderFactory factory

    def setupSpec() {
        Configuration config = Configuration.instance
        String fakeDependencyApiUrl = config.getValue("fakeDependencyApiUrl")
        String immunizationDeciderApiUrl = config.getValue("immunizationDeciderApiUrl")
        ImmunizationDeciderProviderFactoryConfig factoryConfig = new ImmunizationDeciderProviderFactoryConfig(fakeDependencyApiUrl, immunizationDeciderApiUrl)
        factory = new ImmunizationDeciderProviderFactory(factoryConfig)
    }

    protected static ImmunizationOccurrence occurrence(Long daysAgo, ImmunizationType type) {
        Instant date = InstantLibraryKt.nowMinusDays(daysAgo)
        new ImmunizationOccurrence(date, type)
    }

}
