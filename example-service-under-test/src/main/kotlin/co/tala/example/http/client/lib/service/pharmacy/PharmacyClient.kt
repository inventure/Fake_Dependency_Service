package co.tala.example.http.client.lib.service.pharmacy

import co.tala.example.http.client.core.ApiResponse
import co.tala.example.http.client.core.IExampleHttpClient
import co.tala.example.http.client.core.apiResponse
import co.tala.example.http.client.lib.builder.IRequestHeaderBuilder
import co.tala.example.http.client.lib.service.immunization_decider.model.ImmunizationDecisionStatusResponse

interface IPharmacyClient {
    fun postImmunizationDecision(request: ImmunizationDecisionStatusResponse): ApiResponse<Unit>
}

class PharmacyClient(
    private val client: IExampleHttpClient,
    private val requestHeaderBuilder: IRequestHeaderBuilder
) : IPharmacyClient {
    override fun postImmunizationDecision(request: ImmunizationDecisionStatusResponse): ApiResponse<Unit> = client.post(
        uri = "/immunizations/decisions",
        headers = requestHeaderBuilder.clear().build(),
        content = request
    ).apiResponse()
}
