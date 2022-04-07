package co.tala.example.http.client.lib.mock

import co.tala.example.http.client.core.ApiResponse
import co.tala.example.http.client.core.IExampleHttpClient
import co.tala.example.http.client.core.apiResponse
import co.tala.example.http.client.lib.builder.IRequestHeaderBuilder
import co.tala.example.http.client.lib.mock.model.MockData
import co.tala.example.http.client.lib.service.user.model.UserResponse

interface IMockUserClient {
    fun setUpGetUser(userId: String, request: MockData<UserResponse>): ApiResponse<MockData<UserResponse>>
    fun verifyGetUser(userId: String): ApiResponse<List<Any>>
}

class MockUserClient(
    private val client: IExampleHttpClient
) : IMockUserClient {
    override fun setUpGetUser(userId: String, request: MockData<UserResponse>): ApiResponse<MockData<UserResponse>> =
        client.post(
            uri = "/users/$userId",
            content = request
        ).apiResponse()

    override fun verifyGetUser(userId: String): ApiResponse<List<Any>> = client.get(
        uri = "/users/$userId"
    ).apiResponse()
}
