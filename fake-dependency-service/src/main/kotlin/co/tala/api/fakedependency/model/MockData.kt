package co.tala.api.fakedependency.model

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.util.CollectionUtils


class MockData(
    val responseBody: Any? = null,
    val responseSetUpMetadata: ResponseSetUpMetadata = ResponseSetUpMetadata(),
    val responseHeaders: Map<String, List<String>> = emptyMap()
) {
    fun toResponseEntity(): ResponseEntity<Any> = ResponseEntity(
        responseBody,
        CollectionUtils.toMultiValueMap(responseHeaders),
        HttpStatus.valueOf(responseSetUpMetadata.httpStatus)
    )
}
