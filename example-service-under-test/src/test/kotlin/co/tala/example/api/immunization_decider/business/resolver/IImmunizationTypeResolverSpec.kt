package co.tala.example.api.immunization_decider.business.resolver

import co.tala.example.specs.core.lib.minusDays
import co.tala.example.specs.core.lib.now
import co.tala.example.http.client.lib.service.immunization_history.model.ImmunizationHistoryResponse
import co.tala.example.http.client.lib.service.immunization_history.model.ImmunizationOccurrence
import co.tala.example.http.client.lib.service.immunization_history.model.ImmunizationType
import co.tala.example.http.client.lib.service.user.model.UserResponse
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import java.time.Instant

class IImmunizationTypeResolverSpec : WordSpec({
    "Covid19ImmunizationTypeResolver" should {
        val sut = Covid19ImmunizationTypeResolver()

        "resolve when user is not of age" {
            val userResponse = UserResponse(dateOfBirth = Instant.now().minusDays(365 * 5))
            val historyResponse = ImmunizationHistoryResponse(
                occurrences = listOf()
            )

            val result = sut.resolve(userResponse, historyResponse)

            result shouldBe null
        }

        "resolve when there is history of 2 dosages and a year has not passed" {
            val userResponse = UserResponse(dateOfBirth = Instant.now().minusDays(365 * 20))
            val historyResponse = ImmunizationHistoryResponse(
                occurrences = listOf(
                    ImmunizationOccurrence(
                        date = now().minusDays(100),
                        type = ImmunizationType.COVID19
                    ),
                    ImmunizationOccurrence(
                        date = now().minusDays(70),
                        type = ImmunizationType.COVID19
                    )
                )
            )

            val result = sut.resolve(userResponse, historyResponse)

            result shouldBe null
        }

        "resolve when there is history of 2 dosages and a year has passed" {
            val userResponse = UserResponse(dateOfBirth = Instant.now().minusDays(365 * 20))
            val historyResponse = ImmunizationHistoryResponse(
                occurrences = listOf(
                    ImmunizationOccurrence(
                        date = now().minusDays(400),
                        type = ImmunizationType.COVID19
                    ),
                    ImmunizationOccurrence(
                        date = now().minusDays(370),
                        type = ImmunizationType.COVID19
                    )
                )
            )

            val result = sut.resolve(userResponse, historyResponse)

            result shouldBe ImmunizationType.COVID19
        }

        "resolve when there is history of 2 dosages and a year has passed 2" {
            val userResponse = UserResponse(dateOfBirth = Instant.now().minusDays(365 * 20))
            val historyResponse = ImmunizationHistoryResponse(
                occurrences = listOf(
                    ImmunizationOccurrence(
                        date = now().minusDays(380),
                        type = ImmunizationType.COVID19
                    ),
                    ImmunizationOccurrence(
                        date = now().minusDays(350),
                        type = ImmunizationType.COVID19
                    )
                )
            )

            val result = sut.resolve(userResponse, historyResponse)

            result shouldBe ImmunizationType.COVID19
        }

        "resolve when there is history of 1 dosage and it is not time for a 2nd dose yet" {
            val userResponse = UserResponse(dateOfBirth = Instant.now().minusDays(365 * 20))
            val historyResponse = ImmunizationHistoryResponse(
                occurrences = listOf(
                    ImmunizationOccurrence(
                        date = now().minusDays(10),
                        type = ImmunizationType.COVID19
                    )
                )
            )

            val result = sut.resolve(userResponse, historyResponse)

            result shouldBe null
        }

        "resolve when there is history of 1 dosage and it is time for a 2nd dose" {
            val userResponse = UserResponse(dateOfBirth = Instant.now().minusDays(365 * 20))
            val historyResponse = ImmunizationHistoryResponse(
                occurrences = listOf(
                    ImmunizationOccurrence(
                        date = now().minusDays(29),
                        type = ImmunizationType.COVID19
                    )
                )
            )

            val result = sut.resolve(userResponse, historyResponse)

            result shouldBe ImmunizationType.COVID19
        }
    }

    "TdapImmunizationTypeResolver" should {
        val sut = TdapImmunizationTypeResolver()

        "resolve when there is history of 1 dosage and 10 years has not passed" {
            val userResponse = UserResponse(dateOfBirth = Instant.now().minusDays(365 * 20))
            val historyResponse = ImmunizationHistoryResponse(
                occurrences = listOf(
                    ImmunizationOccurrence(
                        date = now().minusDays(100),
                        type = ImmunizationType.TDAP
                    )
                )
            )

            val result = sut.resolve(userResponse, historyResponse)

            result shouldBe null
        }

        "resolve when there is history of 1 dosage and 10 years has passed" {
            val userResponse = UserResponse(dateOfBirth = Instant.now().minusDays(365 * 20))
            val historyResponse = ImmunizationHistoryResponse(
                occurrences = listOf(
                    ImmunizationOccurrence(
                        date = now().minusDays(365 * 11),
                        type = ImmunizationType.TDAP
                    )
                )
            )

            val result = sut.resolve(userResponse, historyResponse)

            result shouldBe ImmunizationType.TDAP
        }
    }

    "InfluenzaImmunizationTypeResolver" should {
        val sut = InfluenzaImmunizationTypeResolver()

        "resolve when there is history of 1 dosage and 1 year has not passed" {
            val userResponse = UserResponse(dateOfBirth = Instant.now().minusDays(365 * 20))
            val historyResponse = ImmunizationHistoryResponse(
                occurrences = listOf(
                    ImmunizationOccurrence(
                        date = now().minusDays(100),
                        type = ImmunizationType.INFLUENZA
                    )
                )
            )

            val result = sut.resolve(userResponse, historyResponse)

            result shouldBe null
        }

        "resolve when there is history of 1 dosage and 1 years has passed" {
            val userResponse = UserResponse(dateOfBirth = Instant.now().minusDays(365 * 20))
            val historyResponse = ImmunizationHistoryResponse(
                occurrences = listOf(
                    ImmunizationOccurrence(
                        date = now().minusDays(400),
                        type = ImmunizationType.INFLUENZA
                    )
                )
            )

            val result = sut.resolve(userResponse, historyResponse)

            result shouldBe ImmunizationType.INFLUENZA
        }
    }
})
