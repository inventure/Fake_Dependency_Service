package co.tala.api.fakedependency.business.provider

import co.tala.api.fakedependency.model.MockData
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

    fun verify(
        request: HttpServletRequest
    ): ResponseEntity<List<Any>>
}
