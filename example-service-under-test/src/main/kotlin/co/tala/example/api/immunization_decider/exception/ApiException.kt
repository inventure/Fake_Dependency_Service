package co.tala.example.api.immunization_decider.exception

import org.springframework.http.HttpStatus

abstract class ApiException(val errorMessage: String, val errorCode: Int, val httpStatus: HttpStatus) : Exception(errorMessage)
