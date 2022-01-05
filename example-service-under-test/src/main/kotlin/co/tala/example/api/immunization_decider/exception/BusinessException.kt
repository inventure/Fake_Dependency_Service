package co.tala.example.api.immunization_decider.exception

abstract class BusinessException(val sourceRefId: String, val errorMessage: String) : Exception(errorMessage)
