package co.tala.api.fakedependency.controller

import co.tala.api.fakedependency.business.provider.IFakeDependencyProvider
import co.tala.api.fakedependency.constant.Constant
import co.tala.api.fakedependency.constant.VerifyMockContent
import co.tala.api.fakedependency.model.MockData
import io.swagger.annotations.Api
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

@RestController
@RequestMapping(
    Constant.MOCK_SERVICE,
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

    @Suppress("UNCHECKED_CAST")
    @GetMapping("**/mock-resources/**")
    fun verify(
        // NOTE: defaults to 'list' for backwards compatibility
        @RequestParam(defaultValue = "list", name = VerifyMockContent.QUERY_PARAM_KEY)
        verifyMockContent: String,
        request: HttpServletRequest
    ): ResponseEntity<Any> = when (VerifyMockContent.valueOf(verifyMockContent.uppercase())) {
        VerifyMockContent.LIST -> provider.verifyList(request)
        VerifyMockContent.LAST -> provider.verifyLast(request)
        VerifyMockContent.DETAILED -> provider.verifyDetailed(request)
    } as ResponseEntity<Any>
}
