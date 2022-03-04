package co.tala.api.fakedependency.business.provider

import co.tala.api.fakedependency.model.MockData
import co.tala.api.fakedependency.model.DetailedRequestPayloads
import org.springframework.http.ResponseEntity
import javax.servlet.http.HttpServletRequest

interface IFakeDependencyProvider {
    fun setup(
        mockData: MockData,
        request: HttpServletRequest
    ): ResponseEntity<MockData>

    fun patchSetup(
        request: HttpServletRequest
    ): ResponseEntity<Unit>

    fun execute(
        request: HttpServletRequest
    ): ResponseEntity<Any>

    fun verifyList(
        request: HttpServletRequest
    ): ResponseEntity<List<Any>>

    fun verifyDetailed(
        request: HttpServletRequest
    ): ResponseEntity<DetailedRequestPayloads>

    fun verifyLast(request: HttpServletRequest): ResponseEntity<ByteArray>
}
