package co.tala.example.specs.immunization_decider

import co.tala.example.http.client.lib.mock.IMockImmunizationHistoryClient
import co.tala.example.http.client.lib.mock.IMockPharmacyClient
import co.tala.example.http.client.lib.mock.IMockUserClient
import co.tala.example.http.client.lib.service.immunization_decider.IImmunizationDeciderClient

data class ImmunizationDeciderClientContainer(
    val mockUser: IMockUserClient,
    val mockPharmacy: IMockPharmacyClient,
    val mockImmunizationHistory: IMockImmunizationHistoryClient,
    val immunizationDecider: IImmunizationDeciderClient
)
