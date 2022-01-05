package co.tala.example.http.client.lib.service.user.model

import java.time.Instant

data class UserResponse(
    val userId: Long? = null,
    val dateOfBirth: Instant? = null,
    val firstName: String? = null,
    val lastName: String? = null
)
