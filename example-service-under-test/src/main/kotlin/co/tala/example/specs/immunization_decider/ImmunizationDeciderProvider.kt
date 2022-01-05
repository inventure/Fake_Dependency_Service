package co.tala.example.specs.immunization_decider

import co.tala.example.specs.core.lib.minusDays
import co.tala.example.specs.core.lib.mockData
import co.tala.example.specs.core.lib.now
import co.tala.example.specs.core.lib.randomHex
import co.tala.example.specs.core.workflo.WorkfloStepProvider
import co.tala.example.http.client.core.ApiResponse
import co.tala.example.http.client.lib.mock.model.MockData
import co.tala.example.http.client.lib.service.immunization_decider.model.ImmunizationDecisionStatus
import co.tala.example.http.client.lib.service.immunization_decider.model.ImmunizationDecisionStatusResponse
import co.tala.example.http.client.lib.service.immunization_decider.model.InitiateImmunizationDecisionRequest
import co.tala.example.http.client.lib.service.immunization_history.model.ImmunizationHistoryResponse
import co.tala.example.http.client.lib.service.immunization_history.model.ImmunizationType
import co.tala.example.http.client.lib.service.user.model.UserResponse
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldMatch

class ImmunizationDeciderProvider(
    private val scenario: ImmunizationDeciderScenario,
    private val client: ImmunizationDeciderClientContainer
) : WorkfloStepProvider(scenario) {
    // MOCKS
    fun setUpGetUser(httpStatus: Int): ApiResponse<MockData<UserResponse>> =
        client.mockUser.setUpGetUser(
            userId = scenario.userId.toString(),
            request = mockData(
                responseBody = UserResponse(
                    userId = scenario.userId,
                    dateOfBirth = now().minusDays(scenario.bornDaysAgo),
                    firstName = randomHex(),
                    lastName = randomHex()
                ),
                httpStatus = httpStatus
            )
        ).httpStatusShouldBe(200)

    fun verifyGetUser(expectedCount: Int): Boolean = verify {
        val actual = client.mockUser.verifyGetUser(userId = scenario.userId.toString()).body.notNull("body").size
        assert("size") { actual shouldBe expectedCount }
    }

    fun setUpGetHistory(httpStatus: Int): ApiResponse<MockData<ImmunizationHistoryResponse>> =
        client.mockImmunizationHistory.setUpGetHistory(
            userId = scenario.userId.toString(),
            request = mockData(
                responseBody = ImmunizationHistoryResponse(
                    occurrences = scenario.immunizationOccurrences
                ),
                httpStatus = httpStatus
            )
        ).httpStatusShouldBe(200)

    fun verifyGetHistory(expectedCount: Int): Boolean = verify {
        val actual = client.mockImmunizationHistory.verifyGetHistory(
            userId = scenario.userId.toString()
        ).httpStatusShouldBe(200).body.notNull("body").size
        assert("size") { actual shouldBe expectedCount }
    }

    fun setUpPostImmunizationDecision(httpStatus: Int): ApiResponse<Unit> =
        client.mockPharmacy.setUpPostImmunizationDecision(
            sourceRefId = scenario.sourceRefId,
            request = mockData(
                responseBody = Unit,
                httpStatus = httpStatus
            )
        ).httpStatusShouldBe(200)

    fun verifyPostImmunizationDecision(expectedCount: Int): ImmunizationDecisionStatusResponse = verifyUntil(10000) {
        val response = client.mockPharmacy.verifyPostImmunizationDecision(
            sourceRefId = scenario.sourceRefId
        ).httpStatusShouldBe(200).body.notNull("body")
        assert { response.size shouldBe expectedCount }
        response.last()
    }

    // MOCKS

    // SERVICE UNDER TEST

    fun initiateDecision(): ApiResponse<Unit> = client.immunizationDecider.initiateDecision(
        request = InitiateImmunizationDecisionRequest(
            sourceRefId = scenario.sourceRefId,
            userId = scenario.userId.toString()
        )
    ).httpStatusShouldBe(202)

    fun getStatus(): ApiResponse<ImmunizationDecisionStatusResponse> =
        client.immunizationDecider.getStatus(sourceRefId = scenario.sourceRefId).httpStatusShouldBe(200)

    fun getStatusUntilCompleted(): ApiResponse<ImmunizationDecisionStatusResponse> = verifyUntil(10000) {
        val response = getStatus().notNull("response")
        assert("status") { response.body?.status.notNull("status") shouldNotBe ImmunizationDecisionStatus.IN_PROGRESS }
        response
    }

    // SERVICE UNDER TEST

    // VERIFICATIONS

    fun verifyImmunizationDecisionStatus(
        response: ImmunizationDecisionStatusResponse?,
        expectedStatus: ImmunizationDecisionStatus,
        expectedAvailableImmunizations: List<ImmunizationType>
    ): Boolean {
        val actual = response.notNull("ImmunizationDecisionStatusResponse")
        return verify {
            assert("status") { actual.status shouldBe expectedStatus }
            assert("availableImmunizations") { actual.availableImmunizations?.sorted() shouldBe expectedAvailableImmunizations.sorted() }
            assert("userId") { actual.userId shouldBe scenario.userId }
            assert("sourceRefId") { actual.sourceRefId shouldBe scenario.sourceRefId }
        }
    }

    fun verifyApiError(
        response: ApiResponse<*>,
        expectedApiError: ImmunizationDeciderApiError,
        argsBlock: (ImmunizationDeciderScenario) -> List<Any>
    ): Boolean {
        val args = argsBlock(scenario).map { it.toString() }.toTypedArray()
        val actualErrorMessage = response.headers["X-Example-Error-Message"]?.first()
        val expectedErrorMessage = expectedApiError.errorMessage.format(*args)
        return verify {
            assert("X-Example-Error-Message") { actualErrorMessage shouldBe expectedErrorMessage }
        }
    }

    fun verifyBusinessError(
        response: ApiResponse<ImmunizationDecisionStatusResponse>,
        expectedBusinessError: ImmunizationDeciderBusinessError,
        argsBlock: (ImmunizationDeciderScenario) -> List<Any>
    ): Boolean {
        val args = argsBlock(scenario).map { it.toString() }.toTypedArray()
        val actualErrorMessage = response.body?.error.notNull("error")
        val expectedErrorMessage = Regex(expectedBusinessError.errorMessageRegex.format(*args))

        verifyImmunizationDecisionStatus(
            response = response.body.notNull("body"),
            expectedStatus = ImmunizationDecisionStatus.FAILURE,
            expectedAvailableImmunizations = emptyList()
        )
        return verify {
            assert("error") { actualErrorMessage shouldMatch expectedErrorMessage }
        }
    }

    fun throwExampleMultiErrorFailure() = verify {
        assert("number") { 1 shouldBe 2 }
        assert("string") { "foo" shouldBe "bar" }
    }

    // VERIFICATIONS
}
