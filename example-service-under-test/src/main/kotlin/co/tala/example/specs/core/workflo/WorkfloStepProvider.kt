package co.tala.example.specs.core.workflo

import co.tala.example.specs.core.validator.ValidationException
import co.tala.example.specs.core.validator.Validator
import co.tala.example.specs.core.lib.now
import co.tala.example.specs.core.validator.ValidationContext
import co.tala.example.http.client.core.ApiResponse
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

abstract class WorkfloStepProvider(private val scenario: Scenario) {
    private val lastResponse: AtomicReference<ApiResponse<*>> = AtomicReference()
    private val shouldAssertHttpResponses: AtomicBoolean = AtomicBoolean(true)


    /**
     * Prevents provider from failing tests when api response status code is not 2XX.
     * Useful for negative testing
     */
    fun disableHttpResponseAssertions() {
        shouldAssertHttpResponses.set(false)
    }

    /**
     * Enables provider to fail tests when api response status code is not 2XX.
     */
    fun enableHttpResponseAssertions() {
        shouldAssertHttpResponses.set(true)
    }

    /**
     * Asserts the response status code
     */
    protected fun <T> ApiResponse<T>.httpStatusShouldBe(statusCode: Int): ApiResponse<T> = apply {
        val response = this
        lastResponse.set(response)
        if (shouldAssertHttpResponses.get()) {
            validator().validateAll {
                assert("httpStatusCode") { response.statusCode shouldBe statusCode }
            }
        }
    }

    protected fun <T> T?.notNull(hint: String?): T {
        verify { assert(hint) { this shouldNotBe null } }
        return this!!
    }

    protected fun verify(block: Validator.() -> Validator): Boolean = validator().validateAll(block)

    protected fun <T> verifyUntil(timeout: Long, block: Validator.() -> T): T = verifyUntil(timeout, 500, block)

    protected fun <T> verifyUntil(timeout: Long, sleep: Long, block: Validator.() -> T): T =
        retryUntil(timeout, sleep) {
            validator().validateAll(block)
        }

    private fun <T> retryUntil(timeout: Long, sleep: Long, action: () -> T): T {
        val start = now().toEpochMilli()
        while (true) {
            try {
                return action()
            } catch (e: Throwable) {
                when (e) {
                    is ValidationException -> {
                        if (now().toEpochMilli() - start >= timeout)
                            throw e
                        runBlocking {
                            delay(sleep)
                        }
                    }
                    else -> throw e
                }
            }
        }
    }

    private fun validator(): Validator = Validator(ValidationContext(lastResponse.get(), scenario))

}
