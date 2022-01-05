package co.tala.example.specs.immunization_decider

import co.tala.example.http.client.core.ApiResponse
import co.tala.example.http.client.lib.service.immunization_decider.model.ImmunizationDecisionStatus
import co.tala.example.http.client.lib.service.immunization_decider.model.ImmunizationDecisionStatusResponse
import spock.lang.Narrative
import spock.lang.Unroll

import static co.tala.example.http.client.lib.service.immunization_history.model.ImmunizationType.COVID19
import static co.tala.example.http.client.lib.service.immunization_history.model.ImmunizationType.INFLUENZA
import static co.tala.example.http.client.lib.service.immunization_history.model.ImmunizationType.TDAP

@Unroll
@Narrative("""
These specs execute the workflow of the Immunization Decider Service and verify the list of available immunizations
for the user under test. The following services are mocked to provide service under test apis to hit to get deterministic
data to compute the decision:
* Users Service: returns the age of the user
* Immunization History Service: returns all vaccinations the user has had
* Pharmacy Service: where Immunization Decider makes a callback to with the decision

The happy path flow is:
* Mock these 3 services to return the deterministic data under various scenario combinations
* Call initiate decision on service under test
* Service under test will then hit the Users Service and Immunization History service to get data.
* Service under test will compute decisions, then post a callback to Pharmacy Service.
* Test will call get status endpoint until it is SUCCESS
* Test will assert decision and all properties of end decision result

Negative tests include:
* Verifying getting a status of non existing source ref id returns 404 with error message in response header
* Verifying that mocks returning non 2XX will mark the decision as FAILED and the error is in the status response. For now, callbacks are not made to Pharmacy Service in this case.
""")
class InitiateDecisionSpec extends ImmunizationDeciderSpec {
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

        and: "a call should be made to users service"
            provider.verifyGetUser(1)

        and: "a call should be made to immunization history service"
            provider.verifyGetHistory(1)

        and: "the immunization status should be #expectedStatus"
        and: "the immunizations available should be #expectedImmunizationsAvailable"
            provider.verifyImmunizationDecisionStatus(actualDecision, expectedStatus, expectedImmunizationsAvailable)

        and: "a callback should be made to pharmacy service with the decision"
            ImmunizationDecisionStatusResponse mockPharmacyCapture = provider.verifyPostImmunizationDecision(1)
            provider.verifyImmunizationDecisionStatus(mockPharmacyCapture, expectedStatus, expectedImmunizationsAvailable)


        where:
            bornDaysAgo | immunizationOccurrences      | expectedImmunizationsAvailable
            // No Vaccination History
            100         | []                           | []
            400         | []                           | [INFLUENZA]
            365 * 11    | []                           | [INFLUENZA, TDAP]
            365 * 20    | []                           | [INFLUENZA, TDAP, COVID19]

            // Vaccination History
            400         | [occurrence(100, INFLUENZA)] | []
            800         | [occurrence(500, INFLUENZA)] | [INFLUENZA]
            365 * 11    | [occurrence(100, INFLUENZA)] | [TDAP]
            365 * 12    | [occurrence(500, INFLUENZA)] | [TDAP, INFLUENZA]
            365 * 13    | [occurrence(500, INFLUENZA),
                           occurrence(1000, TDAP)]     | [INFLUENZA]
            365 * 25    | [occurrence(30, COVID19)]    | [TDAP, INFLUENZA, COVID19]
            365 * 25    | [occurrence(25, COVID19)]    | [TDAP, INFLUENZA]
            365 * 25    | [occurrence(25, COVID19),
                           occurrence(365 * 9, TDAP)]  | [INFLUENZA]
            365 * 25    | [occurrence(25, COVID19),
                           occurrence(365 * 9, TDAP),
                           occurrence(200, INFLUENZA)] | []
            365 * 25    | [occurrence(25, COVID19),
                           occurrence(365 * 9, TDAP),
                           occurrence(400, INFLUENZA)] | [INFLUENZA]
            365 * 25    | [occurrence(35, COVID19),
                           occurrence(365 * 9, TDAP),
                           occurrence(400, INFLUENZA)] | [COVID19, INFLUENZA]
            365 * 25    | [occurrence(35, COVID19),
                           occurrence(70, COVID19),
                           occurrence(365 * 11, TDAP),
                           occurrence(400, INFLUENZA)] | [TDAP, INFLUENZA]
    }

    def "get status should return http status 404 if the source ref id does not exist"() {
        given:
            ImmunizationDeciderApiError expectedError = ImmunizationDeciderApiError.REQUEST_NOT_FOUND
            ImmunizationDeciderProvider provider = factory.create(0, [])
            provider.disableHttpResponseAssertions()

        when: "get status is called with a source ref id that does not exist"
            ApiResponse<ImmunizationDecisionStatusResponse> response = provider.getStatus()

        then: "the api error response should be #expectedError"
            provider.verifyApiError(response, expectedError) {
                [it.sourceRefId]
            }
    }

    def "get status should return business error if external services return non 2XX"() {
        given: "user service is mocked to return #userStatusCode"
        and: "immunization history service is mocked to return #historyStatusCode"
        and: "pharmacy service is mocked to return #pharmacyStatusCode"
            ImmunizationDeciderBusinessError expectedError = ImmunizationDeciderBusinessError.EXTERNAL_REQUEST_FAILED
            ImmunizationDeciderProvider provider = factory.create(0, [])
            provider.setUpGetUser(userStatusCode)
            provider.setUpGetHistory(historyStatusCode)
            provider.setUpPostImmunizationDecision(pharmacyStatusCode)

        when: "an immunization decision is initiated"
            provider.initiateDecision()

        and: "the decision completes"
            ApiResponse<ImmunizationDecisionStatusResponse> actualDecision = provider.getStatusUntilCompleted()

        then: "the business error should be #expectedError"
            provider.verifyBusinessError(actualDecision, expectedError) {
                [it.sourceRefId, 500]
            }

        where:
            userStatusCode | historyStatusCode | pharmacyStatusCode
            500            | 200               | 200
            200            | 500               | 200
            200            | 200               | 500
    }
}
