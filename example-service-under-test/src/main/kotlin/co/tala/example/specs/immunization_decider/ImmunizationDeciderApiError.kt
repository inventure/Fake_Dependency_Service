package co.tala.example.specs.immunization_decider

enum class ImmunizationDeciderApiError(val errorMessage: String, val httpStatus: Int) {
    REQUEST_NOT_FOUND("Decision request not found for sourceRefId '%s'.", 404)
}
