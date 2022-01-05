package co.tala.example.specs.core.validator

import co.tala.example.specs.core.workflo.Scenario
import co.tala.example.http.client.core.ApiResponse

data class ValidationContext(
    val lastApiResponse: ApiResponse<*>?,
    val scenario: Scenario
)
