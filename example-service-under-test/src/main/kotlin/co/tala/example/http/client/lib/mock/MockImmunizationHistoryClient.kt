package co.tala.example.http.client.lib.mock

import co.tala.example.http.client.core.ApiResponse
import co.tala.example.http.client.core.IExampleHttpClient
import co.tala.example.http.client.core.apiResponse
import co.tala.example.http.client.lib.builder.IQueryParamBuilder
import co.tala.example.http.client.lib.builder.IRequestHeaderBuilder
import co.tala.example.http.client.lib.mock.model.MockData
import co.tala.example.http.client.lib.service.immunization_history.model.ImmunizationHistoryResponse

interface IMockImmunizationHistoryClient {
    fun setUpGetHistory(
        userId: String,
        request: MockData<ImmunizationHistoryResponse>
    ): ApiResponse<MockData<ImmunizationHistoryResponse>>

    fun verifyGetHistory(userId: String): ApiResponse<List<Any>>
}

class MockImmunizationHistoryClient(
    private val client: IExampleHttpClient,
    private val queryParamBuilder: IQueryParamBuilder
) : IMockImmunizationHistoryClient {
    override fun setUpGetHistory(
        userId: String,
        request: MockData<ImmunizationHistoryResponse>
    ): ApiResponse<MockData<ImmunizationHistoryResponse>> = client.post(
        uri = "/immunizations${queryParamBuilder.clear().addParam("userId", userId).build()}",
        content = request
    ).apiResponse()

    override fun verifyGetHistory(userId: String): ApiResponse<List<Any>> = client.get(
        uri = "/immunizations${queryParamBuilder.clear().addParam("userId", userId).build()}"
    ).apiResponse()
}
