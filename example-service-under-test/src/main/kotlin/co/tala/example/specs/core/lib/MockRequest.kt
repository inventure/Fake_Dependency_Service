package co.tala.example.specs.core.lib

import co.tala.example.http.client.lib.mock.model.MockData

fun <T> mockData(responseBody: T, httpStatus: Int): MockData<T> = mockData(responseBody, httpStatus, 0)

fun <T> mockData(responseBody: T, httpStatus: Int, delayMs: Long): MockData<T> = MockData(
    responseBody = if (httpStatus in 200..299) responseBody else null,
    responseSetUpMetadata = MockData.ResponseSetUpMetadata(
        httpStatus = httpStatus,
        delayMs = delayMs
    )
)
