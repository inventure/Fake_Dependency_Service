package co.tala.example.http.client.lib.service.immunization_history.model

import java.time.Instant

data class ImmunizationOccurrence(
    val date: Instant? = null,
    val type: ImmunizationType? = null
)
