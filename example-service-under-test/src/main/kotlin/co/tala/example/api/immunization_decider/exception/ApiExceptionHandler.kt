package co.tala.example.api.immunization_decider.exception

import co.tala.api.immunization_decider.constant.X_EXAMPLE_ERROR_CODE
import co.tala.api.immunization_decider.constant.X_EXAMPLE_ERROR_MESSAGE
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class ApiExceptionHandler : ResponseEntityExceptionHandler() {
    @ExceptionHandler(Throwable::class)
    fun handleException(
        ex: Exception,
    ): ResponseEntity<Unit> {
        data class Response(val errorCode: Int, val errorMessage: String, val httpStatus: HttpStatus)

        val response = when (ex) {
            is ApiException -> Response(
                errorCode = ex.errorCode,
                errorMessage = ex.errorMessage,
                httpStatus = ex.httpStatus
            )
            else -> Response(
                errorCode = 50000,
                errorMessage = ex.message ?: "",
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR
            )
        }
        return ResponseEntity
            .status(response.httpStatus)
            .header(X_EXAMPLE_ERROR_CODE, response.errorCode.toString())
            .header(X_EXAMPLE_ERROR_MESSAGE, response.errorMessage)
            .build()
    }

}
