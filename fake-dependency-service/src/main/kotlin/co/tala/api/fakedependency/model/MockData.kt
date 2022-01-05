package co.tala.api.fakedependency.model

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity


class MockData(
    val responseBody: Any? = null,
    val responseSetUpMetadata: ResponseSetUpMetadata = ResponseSetUpMetadata(),
) {
    fun toResponseEntity(): ResponseEntity<Any> = ResponseEntity(
        responseBody,
        HttpStatus.valueOf(responseSetUpMetadata.httpStatus)
    )
}
