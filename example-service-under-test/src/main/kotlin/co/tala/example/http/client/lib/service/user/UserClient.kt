package co.tala.example.http.client.lib.service.user

import co.tala.example.http.client.core.ApiResponse
import co.tala.example.http.client.core.IExampleHttpClient
import co.tala.example.http.client.core.apiResponse
import co.tala.example.http.client.lib.builder.IRequestHeaderBuilder
import co.tala.example.http.client.lib.service.user.model.UserResponse

interface IUserClient {
    fun getUser(userId: String): ApiResponse<UserResponse>
}

class UserClient(
    private val client: IExampleHttpClient
) : IUserClient {
    override fun getUser(userId: String): ApiResponse<UserResponse> = client.get(uri = "/users/$userId").apiResponse()
}
