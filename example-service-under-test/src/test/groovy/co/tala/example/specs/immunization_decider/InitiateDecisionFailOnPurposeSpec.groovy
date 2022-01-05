package co.tala.example.specs.immunization_decider


import co.tala.example.http.client.lib.service.immunization_decider.model.ImmunizationDecisionStatus
import co.tala.example.http.client.lib.service.immunization_decider.model.ImmunizationDecisionStatusResponse
import spock.lang.Ignore
import spock.lang.Narrative
import spock.lang.Unroll

import static co.tala.example.http.client.lib.service.immunization_history.model.ImmunizationType.COVID19
import static co.tala.example.http.client.lib.service.immunization_history.model.ImmunizationType.INFLUENZA
import static co.tala.example.http.client.lib.service.immunization_history.model.ImmunizationType.TDAP

@Unroll
@Narrative("""
These specs show how the WorkfloStepProvider frameworks captures and throws assertion errors.
Remove the @Ignore annotation to run and see the failures
""")
@Ignore
class InitiateDecisionFailOnPurposeSpec extends ImmunizationDeciderSpec {
    def "initiate decision should succeed and produce correct results"() {
        given: "user service is mocked to return 200"
        and: "immunization history service is mocked to return 200"
        and: "pharmacy service is mocked to return 200"
            ImmunizationDecisionStatus expectedStatus = ImmunizationDecisionStatus.SUCCESS
            ImmunizationDeciderProvider provider = factory.create(bornDaysAgo, immunizationOccurrences)
            provider.setUpGetUser(200)
            provider.setUpGetHistory(200)
            provider.setUpPostImmunizationDecision(200)

        when: "an immunization decision is initiated"
            provider.initiateDecision()

        then: "the response status code should be 202"

        when: "the decision completes"
            ImmunizationDecisionStatusResponse actualDecision = provider.getStatusUntilCompleted().body

        then: "the response status code should be 200"

        and: "the immunization status should be #expectedStatus"
        and: "the immunizations available should be #expectedImmunizationsAvailable"
            provider.verifyImmunizationDecisionStatus(actualDecision, expectedStatus, expectedImmunizationsAvailable)


        where:
            bornDaysAgo | immunizationOccurrences      | expectedImmunizationsAvailable
            // Test Failure Examples
            365 * 20    | []                           | []
            365 * 11    | [occurrence(100, INFLUENZA)] | [TDAP, COVID19]
            100         | []                           | [INFLUENZA, TDAP, COVID19]
    }

    def "throw exception on purpose example"() {
        given:
            ImmunizationDeciderProvider provider = factory.create(0, [])

        when:
            provider.throwExampleMultiErrorFailure()

        then:
            true

    }
}
