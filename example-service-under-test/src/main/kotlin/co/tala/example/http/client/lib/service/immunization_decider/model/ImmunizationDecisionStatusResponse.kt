package co.tala.example.http.client.lib.service.immunization_decider.model

import co.tala.example.http.client.lib.service.immunization_history.model.ImmunizationType
import java.time.Instant

data class ImmunizationDecisionStatusResponse(
    val sourceRefId: String? = null,
    val userId: Long? = null,
    val status: ImmunizationDecisionStatus? = null,
    val availableImmunizations: List<ImmunizationType>? = null,
    val startedAt: Instant? = null,
    val finishedAt: Instant? = null,
    val error: String? = null
)
