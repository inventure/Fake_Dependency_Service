package co.tala.api.fakedependency.model

import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.util.CollectionUtils
import javax.validation.constraints.NotNull


class MockData(
    val responseBody: Any? = null,
    @field: NotNull
    val responseSetUpMetadata: ResponseSetUpMetadata = ResponseSetUpMetadata(),
    @field: NotNull
    val responseHeaders: Map<String, List<String>> = emptyMap()
) {
    fun toResponseEntity(): ResponseEntity<Any> {
        return ResponseEntity
            .status(responseSetUpMetadata.httpStatus)
            .headers(HttpHeaders(CollectionUtils.toMultiValueMap(responseHeaders)))
            .body(responseBody)
    }
}
