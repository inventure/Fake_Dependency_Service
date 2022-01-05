package co.tala.example.http.client.lib.auth

interface IAuthenticator {
    fun getToken(): AuthToken
}
