package co.tala.example.http.client.lib.factory


import co.tala.example.http.client.core.ExampleHttpClient
import co.tala.example.http.client.lib.auth.IAuthenticator
import co.tala.example.http.client.lib.builder.QueryParamBuilder
import co.tala.example.http.client.lib.builder.RequestHeaderBuilder
import co.tala.example.http.client.lib.converter.InstantConverter
import co.tala.example.http.client.lib.converter.InstantConverterTimePrecision
import co.tala.example.http.client.lib.mock.*
import co.tala.example.http.client.lib.service.immunization_decider.IImmunizationDeciderClient
import co.tala.example.http.client.lib.service.immunization_decider.ImmunizationDeciderClient
import co.tala.example.http.client.lib.service.immunization_history.IImmunizationHistoryClient
import co.tala.example.http.client.lib.service.immunization_history.ImmunizationHistoryClient
import co.tala.example.http.client.lib.service.pharmacy.IPharmacyClient
import co.tala.example.http.client.lib.service.pharmacy.PharmacyClient
import co.tala.example.http.client.lib.service.user.IUserClient
import co.tala.example.http.client.lib.service.user.UserClient
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import java.time.Instant

interface IClientFactory<T> {
    fun create(baseUrl: String? = null, authenticator: IAuthenticator? = null, requestId: String? = null): T
}

// TODO AUTH
class ClientFactory<T>(
    private val clazz: Class<T>,
    private val baseUrl: String,
    private val authenticator: IAuthenticator? = null,
    private val requestId: String? = null,
    private val instantConverterTimePrecision: InstantConverterTimePrecision = InstantConverterTimePrecision.PRECISION6
) : IClientFactory<T> {
    @Suppress("IMPLICIT_CAST_TO_ANY")
    override fun create(
        baseUrl: String?,
        authenticator: IAuthenticator?,
        requestId: String?,
    ): T {
        val gson: Gson = GsonBuilder().registerTypeAdapter(
            Instant::class.java,
            InstantConverter(instantConverterTimePrecision)
        ).create()
        val requestBuilder = RequestHeaderBuilder(
            authenticator = authenticator ?: this.authenticator,
            requestId = requestId ?: this.requestId
        )
        val queryBuilder = QueryParamBuilder(gson = gson)
        val okHttpClient = OkHttpClient()
        val client = ExampleHttpClient(
            okHttpClient = okHttpClient,
            requestHeaderBuilder = requestBuilder,
            baseUrl = baseUrl ?: this.baseUrl,
            gson = gson
        )

        @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
        return when (clazz) {
            IMockUserClient::class.java -> MockUserClient(client)
            IMockImmunizationHistoryClient::class.java -> MockImmunizationHistoryClient(
                client,
                queryBuilder
            )
            IMockPharmacyClient::class.java -> MockPharmacyClient(client, queryBuilder)
            IUserClient::class.java -> UserClient(client)
            IImmunizationDeciderClient::class.java -> ImmunizationDeciderClient(client, queryBuilder)
            IImmunizationHistoryClient::class.java -> ImmunizationHistoryClient(client, queryBuilder)
            IPharmacyClient::class.java -> PharmacyClient(client)
            else -> throw Exception("class '${clazz}' is not a valid client!")
        } as T
    }
}

inline fun <reified T : Any> createClient(
    baseUrl: String,
    authenticator: IAuthenticator? = null,
    requestId: String? = null,
    instantConverterTimePrecision: InstantConverterTimePrecision = InstantConverterTimePrecision.PRECISION6
): T = ClientFactory(
    clazz = T::class.java,
    baseUrl = baseUrl,
    authenticator = authenticator,
    requestId = requestId,
    instantConverterTimePrecision = instantConverterTimePrecision
).create()
