package co.tala.api.fakedependency.controller

import co.tala.api.fakedependency.business.provider.IFakeDependencyProvider
import co.tala.api.fakedependency.constant.BaseUrl
import co.tala.api.fakedependency.model.MockData
import io.swagger.annotations.Api
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

@RestController
@RequestMapping(
    BaseUrl.FAKE_DEPENDENCY,
    produces = ["application/json"]
)
@Api(value = "Fake Dependency API", description = "A mock for any API dependency")
class FakeDependencyServiceController(private val provider: IFakeDependencyProvider) {
    @PostMapping("**/mock-resources/**")
    fun setUp(
        @Valid @RequestBody mockData: MockData,
        request: HttpServletRequest
    ): ResponseEntity<MockData> = provider.setup(mockData, request)

    @PatchMapping("**/mock-resources/**")
    fun patchSetup(
        request: HttpServletRequest
    ): ResponseEntity<Unit> = provider.patchSetup(request)

    @PostMapping("**")
    fun post(
        request: HttpServletRequest
    ): ResponseEntity<Any> = provider.execute(request)

    @PutMapping("**")
    fun put(
        request: HttpServletRequest
    ): ResponseEntity<Any> = provider.execute(request)

    @PatchMapping("**")
    fun patch(
        request: HttpServletRequest
    ): ResponseEntity<Any> = provider.execute(request)

    @GetMapping("**")
    fun get(
        request: HttpServletRequest
    ): ResponseEntity<Any> = provider.execute(request)

    @DeleteMapping("**")
    fun delete(
        request: HttpServletRequest
    ): ResponseEntity<Any> = provider.execute(request)

    @GetMapping("**/mock-resources/**")
    fun verify(
        request: HttpServletRequest
    ): ResponseEntity<List<Any>> = provider.verify(request)
}
