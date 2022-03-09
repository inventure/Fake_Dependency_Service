package co.tala.api.fakedependency.model

import org.springframework.http.HttpStatus
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
    fun toResponseEntity(): ResponseEntity<Any> = ResponseEntity(
        responseBody,
        CollectionUtils.toMultiValueMap(responseHeaders),
        HttpStatus.valueOf(responseSetUpMetadata.httpStatus)
    )
}
