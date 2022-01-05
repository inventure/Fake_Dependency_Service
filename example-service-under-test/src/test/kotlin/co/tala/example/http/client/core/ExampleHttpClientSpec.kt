package co.tala.example.http.client.core


import com.google.gson.Gson
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import okhttp3.*
import java.io.InputStream
import java.net.URL
import java.time.Instant
import kotlin.random.Random

class ExampleHttpClientSpec : WordSpec({
    val gson = Gson()
    val baseUrl = "http://some-base-url"

    data class TestModel(
        val integer: Int,
        val long: Long,
        val string: String,
        val list: List<String>
    )

    fun testModel(): TestModel = TestModel(
        integer = Random.nextInt(),
        long = Random.nextLong(),
        string = Random.nextInt().toString(),
        list = listOf(Random.nextInt().toString(), Random.nextInt().toString())
    )

    val someResponse = testModel()
    val someRequest = testModel()
    val someHeaders = mapOf("foo" to "bar")
    val someMultiMap = mapOf("foo1" to listOf("bar1"))
    val serializedResponse: String = gson.toJson(someResponse)

    fun rawResponse(method: ExampleHttpMethod): RawResponse {
        val okHttpResponseMock: Response = mockk()
        val responseBodyMock: ResponseBody = mockk()
        val streamMock: InputStream = mockk()
        val bytes = serializedResponse.toByteArray()
        val requestMock: Request = mockk()
        val headersMock: Headers = mockk()
        val urlMock: HttpUrl = mockk()
        val fakeUrl: URL = URL("http", "someHost", 1234, "/someResource")

        every { streamMock.readAllBytes() } returns bytes
        every { responseBodyMock.byteStream() } returns streamMock
        every { okHttpResponseMock.body } returns responseBodyMock
        every { okHttpResponseMock.code } returns 200
        every { headersMock.toMultimap() } returns someMultiMap
        every { okHttpResponseMock.headers } returns headersMock
        every { urlMock.toUrl() } returns fakeUrl
        every { requestMock.url } returns urlMock
        every { requestMock.method } returns method.name
        every { okHttpResponseMock.request } returns requestMock

        return RawResponse(
            gson = gson,
            okHttpResponse = okHttpResponseMock,
            start = Instant.now(),
            end = Instant.now()
        )
    }

    fun clientMock(method: ExampleHttpMethod): OkHttpClient {
        val clientMock: OkHttpClient = mockk()
        val rawResponse = rawResponse(method)
        val callMock: Call = mockk()
        every {
            callMock.execute()
        } returns rawResponse.okHttpResponse

        every {
            clientMock.newCall(any())
        } returns callMock

        return clientMock
    }

    "send request" should {
        ExampleHttpMethod.values().forEach { method ->
            "send a $method request" {
                val uri = "/some-uri"
                val clientMock = clientMock(method)
                val sut: IExampleHttpClient = ExampleHttpClient(clientMock, baseUrl, gson)

                val response: ApiResponse<TestModel> = when (method) {
                    ExampleHttpMethod.POST -> sut.post(
                        uri = uri,
                        headers = someHeaders,
                        content = someRequest
                    )
                    ExampleHttpMethod.PUT -> sut.put(
                        uri = uri,
                        headers = someHeaders,
                        content = someRequest
                    )
                    ExampleHttpMethod.PATCH -> sut.patch(
                        uri = uri,
                        headers = someHeaders,
                        content = someRequest
                    )
                    ExampleHttpMethod.GET -> sut.get(
                        uri = uri,
                        headers = someHeaders
                    )
                    ExampleHttpMethod.DELETE -> sut.delete(
                        uri = uri,
                        headers = someHeaders
                    )
                }.apiResponse()

                response.method shouldBe method
                response.url shouldBe "http://someHost:1234/someResource"
                response.headers shouldBe someMultiMap
                response.isSuccessful shouldBe true
                response.statusCode shouldBe 200
                response.body shouldNotBe null
                val actualBody = response.body!!
                actualBody.integer shouldBe someResponse.integer
                actualBody.long shouldBe someResponse.long
                actualBody.string shouldBe someResponse.string
                actualBody.list shouldBe someResponse.list
            }
        }

        "return a null response on failed deserialization" {
            val uri = "/some-uri"
            val clientMock = clientMock(ExampleHttpMethod.POST)
            val sut: IExampleHttpClient = ExampleHttpClient(clientMock, baseUrl, gson)

            val response = sut.post(
                uri = uri,
                headers = someHeaders,
                content = someRequest
            ).apiResponse<Long>()

            response.body shouldBe null
        }
    }

})
