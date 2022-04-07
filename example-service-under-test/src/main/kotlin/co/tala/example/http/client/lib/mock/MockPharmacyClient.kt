package co.tala.example.http.client.lib.mock

import co.tala.example.http.client.core.ApiResponse
import co.tala.example.http.client.core.IExampleHttpClient
import co.tala.example.http.client.core.apiResponse
import co.tala.example.http.client.lib.builder.IQueryParamBuilder
import co.tala.example.http.client.lib.builder.IRequestHeaderBuilder
import co.tala.example.http.client.lib.mock.model.MockData
import co.tala.example.http.client.lib.service.immunization_decider.model.ImmunizationDecisionStatusResponse

interface IMockPharmacyClient {
    fun setUpPostImmunizationDecision(sourceRefId: String, request: MockData<Unit>): ApiResponse<Unit>
    fun verifyPostImmunizationDecision(sourceRefId: String): ApiResponse<List<ImmunizationDecisionStatusResponse>>
}

class MockPharmacyClient(
    private val client: IExampleHttpClient,
    private val queryParamBuilder: IQueryParamBuilder
) : IMockPharmacyClient {
    override fun setUpPostImmunizationDecision(sourceRefId: String, request: MockData<Unit>): ApiResponse<Unit> =
        client.post(
            uri = "/immunizations/decisions${sourceRefIdQuery(sourceRefId)}",
            content = request
        ).apiResponse()

    override fun verifyPostImmunizationDecision(sourceRefId: String): ApiResponse<List<ImmunizationDecisionStatusResponse>> =
        client.get(
            uri = "/immunizations/decisions${sourceRefIdQuery(sourceRefId)}"
        ).apiResponse()

    private fun sourceRefIdQuery(sourceRefId: String) =
        queryParamBuilder.clear().addParam("sourceRefId", sourceRefId).build()

}
