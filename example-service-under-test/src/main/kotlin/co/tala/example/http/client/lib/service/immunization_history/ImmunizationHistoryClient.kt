package co.tala.example.http.client.lib.service.immunization_history

import co.tala.example.http.client.core.ApiResponse
import co.tala.example.http.client.core.IExampleHttpClient
import co.tala.example.http.client.core.apiResponse
import co.tala.example.http.client.lib.builder.IQueryParamBuilder
import co.tala.example.http.client.lib.builder.IRequestHeaderBuilder
import co.tala.example.http.client.lib.service.immunization_history.model.ImmunizationHistoryResponse

interface IImmunizationHistoryClient {
    fun getHistory(userId: String): ApiResponse<ImmunizationHistoryResponse>
}

class ImmunizationHistoryClient(
    private val client: IExampleHttpClient,
    private val requestHeaderBuilder: IRequestHeaderBuilder,
    private val queryParamBuilder: IQueryParamBuilder
) : IImmunizationHistoryClient {
    override fun getHistory(userId: String): ApiResponse<ImmunizationHistoryResponse> = client.get(
        uri = "/immunizations${queryParamBuilder.clear().addParam("userId", userId).build()}",
        headers = requestHeaderBuilder.clear().build()
    ).apiResponse()

}
