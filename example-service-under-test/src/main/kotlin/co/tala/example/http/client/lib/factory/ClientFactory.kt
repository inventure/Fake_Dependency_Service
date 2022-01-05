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
            baseUrl = baseUrl ?: this.baseUrl,
            gson = gson
        )

        @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
        return when (clazz) {
            IMockUserClient::class.java -> MockUserClient(client, requestBuilder)
            IMockImmunizationHistoryClient::class.java -> MockImmunizationHistoryClient(
                client,
                requestBuilder,
                queryBuilder
            )
            IMockPharmacyClient::class.java -> MockPharmacyClient(client, requestBuilder, queryBuilder)
            IUserClient::class.java -> UserClient(client, requestBuilder)
            IImmunizationDeciderClient::class.java -> ImmunizationDeciderClient(client, requestBuilder, queryBuilder)
            IImmunizationHistoryClient::class.java -> ImmunizationHistoryClient(client, requestBuilder, queryBuilder)
            IPharmacyClient::class.java -> PharmacyClient(client, requestBuilder)
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
