package co.tala.example.http.client.core


import co.tala.example.http.client.core.ExampleHttpMethod.*
import co.tala.example.http.client.core.request.*
import co.tala.example.http.client.lib.builder.IRequestHeaderBuilder
import com.google.gson.Gson
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldMatch
import io.mockk.*
import okhttp3.*
import okhttp3.Call
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
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

    data class SomeMultipartFormDataBodyRequest(
        val foo: String,
        val bar: String,
        val file: String
    ) : MultipartFormDataBodyRequest {
        override fun toRequestBody(): MultipartBody = build {
            tryAddFormData("foo", foo)
            tryAddFormData("bar", bar)
            tryAddFormData("file", "/someFile", file.toRequestBody())
        }
    }

    data class SomeFormBodyRequest(
        val foo: String,
        val bar: String
    ) : FormBodyRequest {
        override fun toRequestBody(): FormBody = build {
            tryAddFormData("foo", foo)
            tryAddFormData("bar", bar)
        }
    }

    data class RequestCapture(
        val contentType: String?,
        val method: String,
        val bodyUtf8: String?
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
    val someMockedHeaders = mapOf("ice" to "cream")
    val someMultiMap = mapOf("foo1" to listOf("bar1"))
    val someMultipartFormDataBodyRequest: MultipartBodyRequest = SomeMultipartFormDataBodyRequest(
        foo = Random.nextInt().toString(),
        bar = Random.nextInt().toString(),
        file = Random.nextInt().toString()
    )
    val someFormBodyRequest: FormBodyRequest = SomeFormBodyRequest(
        foo = Random.nextInt().toString(),
        bar = Random.nextInt().toString()
    )
    val serializedResponse: String = gson.toJson(listOf(someResponse))

    fun createRawResponse(
        method: ExampleHttpMethod,
        serializedResponse: String,
        someMultiMap: Map<String, List<String>>
    ): RawResponse {
        val okHttpResponseMock: Response = mockk()
        val responseBodyMock: ResponseBody = mockk()
        val streamMock: InputStream = mockk()
        val bytes = serializedResponse.toByteArray()
        val requestMock: Request = mockk()
        val headersMock: Headers = mockk()
        val urlMock: HttpUrl = mockk()
        val fakeUrl = URL("http", "someHost", 1234, "/someResource")

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
            gson = Gson(),
            okHttpResponse = okHttpResponseMock,
            start = Instant.now(),
            end = Instant.now()
        )
    }

    fun requestHeaderBuilderMock(returnedHeaders: Map<String, String>): IRequestHeaderBuilder {
        val builderMock = mockk<IRequestHeaderBuilder>()
        every {
            builderMock.addHeader(any(), any())
        } returns builderMock
        every {
            builderMock.clear()
        } returns builderMock
        every {
            builderMock.build()
        } returns returnedHeaders
        return builderMock
    }

    fun clientMock(method: ExampleHttpMethod, assertion: (RequestCapture) -> Unit): OkHttpClient {
        val slot: CapturingSlot<Request> = slot()
        val clientMock: OkHttpClient = mockk()
        val rawResponse = createRawResponse(method, serializedResponse, someMultiMap)
        val callMock: Call = mockk()
        every {
            callMock.execute()
        } returns rawResponse.okHttpResponse

        every {
            clientMock.newCall(request = capture(slot))
        } answers {
            val capture = slot.captured
            Buffer().use { sink ->
                capture.body?.writeTo(sink)
                assertion(
                    RequestCapture(
                        contentType = capture.body?.contentType()?.toString(),
                        method = capture.method,
                        bodyUtf8 = sink.readUtf8()
                    )
                )
            }
            callMock
        }

        return clientMock
    }

    "send request" should {
        values().forEach { method ->
            val specName = when (method) {
                POST, PUT, PATCH -> "succeed for a $method with json request body"
                GET, DELETE -> "succeed for a $method with no request body"
            }
            specName {
                val uri = "/some-uri"
                val clientMock = clientMock(method) { requestCapture ->
                    requestCapture.method shouldBe method.name
                    requestCapture.contentType shouldBe when (method) {
                        POST, PATCH, PUT -> ContentType.APPLICATION_JSON.value + "; charset=utf-8"
                        GET, DELETE -> null
                    }
                    requestCapture.bodyUtf8 shouldBe when (method) {
                        POST, PATCH, PUT -> gson.toJson(someRequest)
                        GET, DELETE -> ""
                    }
                }
                val requestHeaderBuilderMock = requestHeaderBuilderMock(someMockedHeaders)
                val sut: IExampleHttpClient = ExampleHttpClient(clientMock, requestHeaderBuilderMock, baseUrl, gson)

                val rawResponse: RawResponse = when (method) {
                    POST -> sut.post(
                        uri = uri,
                        headers = someHeaders,
                        content = someRequest
                    )
                    PUT -> sut.put(
                        uri = uri,
                        headers = someHeaders,
                        content = someRequest
                    )
                    PATCH -> sut.patch(
                        uri = uri,
                        headers = someHeaders,
                        content = someRequest
                    )
                    GET -> sut.get(
                        uri = uri,
                        headers = someHeaders
                    )
                    DELETE -> sut.delete(
                        uri = uri,
                        headers = someHeaders
                    )
                }
                // Verify deserialization to data class
                val response: ApiResponse<List<TestModel>> = rawResponse.apiResponse()

                response.method shouldBe method
                response.url shouldBe "http://someHost:1234/someResource"
                response.headers shouldBe someMultiMap
                response.isSuccessful shouldBe true
                response.statusCode shouldBe 200
                response.body shouldNotBe null
                val actualBody = response.body!!
                actualBody.first().integer shouldBe someResponse.integer
                actualBody.first().long shouldBe someResponse.long
                actualBody.first().string shouldBe someResponse.string
                actualBody.first().list shouldBe someResponse.list

                // Verify as ByteArray with no deserialization
                val responseBytes = rawResponse.apiResponse<ByteArray>()
                val expectedBytes = serializedResponse.toByteArray()
                val actualBytes = responseBytes.body!!
                actualBytes shouldBe expectedBytes

                verifyOrder {
                    requestHeaderBuilderMock.clear()
                    requestHeaderBuilderMock.build()
                }
            }
        }

        "return a null response on failed deserialization" {
            val uri = "/some-uri"
            val clientMock = clientMock(POST) {}
            val requestHeaderBuilderMock = requestHeaderBuilderMock(someMockedHeaders)
            val sut: IExampleHttpClient = ExampleHttpClient(clientMock, requestHeaderBuilderMock, baseUrl, gson)

            val response = sut.post(
                uri = uri,
                headers = someHeaders,
                content = someRequest
            ).apiResponse<Long>()

            response.body shouldBe null
        }

        "succeed for a multi part form data request" {
            val uri = "/some-uri"
            val clientMock = clientMock(POST) { requestCapture ->
                requestCapture shouldNotBe null
                requestCapture.method shouldBe "POST"
                requestCapture.contentType!! shouldMatch Regex("""${ContentType.MULTIPART_FORM_DATA.value}; (.*)""")
            }
            val requestHeaderBuildMock = requestHeaderBuilderMock(mapOf("foo" to "bar"))
            val sut: IExampleHttpClient = ExampleHttpClient(clientMock, requestHeaderBuildMock, baseUrl, gson)

            sut.post(
                uri = uri,
                content = someMultipartFormDataBodyRequest
            )
        }

        "succeed for a form request" {
            val uri = "/some-uri"
            val clientMock = clientMock(POST) { requestCapture ->
                requestCapture.contentType shouldBe ContentType.APPLICATION_X_WWW_FORM_URLENCODED.value
            }
            val requestHeaderBuildMock = requestHeaderBuilderMock(mapOf("foo" to "bar"))
            val sut: IExampleHttpClient = ExampleHttpClient(clientMock, requestHeaderBuildMock, baseUrl, gson)

            sut.post(
                uri = uri,
                content = someFormBodyRequest
            )
        }

        "succeed for a byte arrary request" {
            val uri = "/some-uri"
            val clientMock = clientMock(POST) { requestCapture ->
                requestCapture.contentType shouldBe ContentType.APPLICATION_OCTET_STREAM.value
                requestCapture.bodyUtf8 shouldBe "123"
            }
            val requestHeaderBuildMock = requestHeaderBuilderMock(mapOf("foo" to "bar"))
            val sut: IExampleHttpClient = ExampleHttpClient(clientMock, requestHeaderBuildMock, baseUrl, gson)

            sut.post(
                uri = uri,
                content = "123".toByteArray()
            )
        }

        "succeed for an octet steam request" {
            val uri = "/some-uri"
            val clientMock = clientMock(PATCH) { requestCapture ->
                requestCapture.contentType shouldBe ContentType.APPLICATION_OCTET_STREAM.value
                requestCapture.bodyUtf8 shouldBe "123"
            }
            val requestHeaderBuildMock = requestHeaderBuilderMock(mapOf("foo" to "bar"))
            val sut: IExampleHttpClient = ExampleHttpClient(clientMock, requestHeaderBuildMock, baseUrl, gson)

            sut.patch(
                uri = uri,
                content = OctetStreamBodyRequest("123".toByteArray())
            )
        }

        "succeed for an offset octet steam request" {
            val uri = "/some-uri"
            val clientMock = clientMock(PATCH) { requestCapture ->
                requestCapture.contentType shouldBe ContentType.APPLICATION_OFFSET_OCTET_STREAM.value
                requestCapture.bodyUtf8 shouldBe "123"
            }
            val requestHeaderBuildMock = requestHeaderBuilderMock(mapOf("foo" to "bar"))
            val sut: IExampleHttpClient = ExampleHttpClient(clientMock, requestHeaderBuildMock, baseUrl, gson)

            sut.patch(
                uri = uri,
                content = OffsetOctetStreamBodyRequest("123".toByteArray())
            )
        }

        "succeed for a string request body" {
            val uri = "/some-uri"
            val clientMock = clientMock(POST) { requestCapture ->
                requestCapture.contentType shouldBe "${ContentType.TEXT_PLAIN.value}; charset=utf-8"
                requestCapture.bodyUtf8 shouldBe "123"
            }
            val requestHeaderBuildMock = requestHeaderBuilderMock(mapOf("foo" to "bar"))
            val sut: IExampleHttpClient = ExampleHttpClient(clientMock, requestHeaderBuildMock, baseUrl, gson)

            sut.post(
                uri = uri,
                content = "123"
            )
        }
    }

})
