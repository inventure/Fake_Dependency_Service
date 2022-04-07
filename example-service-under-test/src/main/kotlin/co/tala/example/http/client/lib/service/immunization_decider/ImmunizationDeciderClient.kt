package co.tala.example.http.client.lib.service.immunization_decider

import co.tala.example.http.client.core.ApiResponse
import co.tala.example.http.client.core.IExampleHttpClient
import co.tala.example.http.client.core.apiResponse
import co.tala.example.http.client.lib.builder.IQueryParamBuilder
import co.tala.example.http.client.lib.builder.IRequestHeaderBuilder
import co.tala.example.http.client.lib.service.immunization_decider.model.ImmunizationDecisionStatusResponse
import co.tala.example.http.client.lib.service.immunization_decider.model.InitiateImmunizationDecisionRequest

interface IImmunizationDeciderClient {
    fun initiateDecision(request: InitiateImmunizationDecisionRequest): ApiResponse<Unit>
    fun getStatus(sourceRefId: String): ApiResponse<ImmunizationDecisionStatusResponse>
}

class ImmunizationDeciderClient(
    private val client: IExampleHttpClient,
    private val queryParamBuilder: IQueryParamBuilder
) : IImmunizationDeciderClient {
    override fun initiateDecision(request: InitiateImmunizationDecisionRequest): ApiResponse<Unit> = client.post(
        uri = "/decisions",
        content = request
    ).apiResponse()

    override fun getStatus(sourceRefId: String): ApiResponse<ImmunizationDecisionStatusResponse> = client.get(
        uri = "/decisions${queryParamBuilder.clear().addParam("sourceRefId", sourceRefId).build()}"
    ).apiResponse()
}
