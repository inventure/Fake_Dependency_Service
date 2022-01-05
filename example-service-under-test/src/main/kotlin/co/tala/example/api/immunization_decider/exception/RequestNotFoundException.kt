package co.tala.example.api.immunization_decider.exception

import org.springframework.http.HttpStatus

class RequestNotFoundException(sourceRefId: String) : ApiException(
    errorMessage = "Decision request not found for sourceRefId '$sourceRefId'.",
    errorCode = 40000,
    httpStatus = HttpStatus.NOT_FOUND
)
