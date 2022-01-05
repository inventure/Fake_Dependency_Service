package co.tala.example.specs.immunization_decider

import co.tala.example.http.client.lib.service.immunization_history.model.ImmunizationOccurrence
import co.tala.example.specs.core.workflo.Scenario

data class ImmunizationDeciderScenario(
    val sourceRefId: String,
    val userId: Long,
    val requestId: String,
    val bornDaysAgo: Long,
    val immunizationOccurrences: List<ImmunizationOccurrence>
) : Scenario
