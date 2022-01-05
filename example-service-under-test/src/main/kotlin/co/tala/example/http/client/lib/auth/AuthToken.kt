package co.tala.example.http.client.lib.auth

data class AuthToken(
    val authorizationType: String,
    val tokenType: String,
    val tokenValue: String
)
