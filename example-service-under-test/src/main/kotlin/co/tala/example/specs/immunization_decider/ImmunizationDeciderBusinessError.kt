package co.tala.example.specs.immunization_decider

enum class ImmunizationDeciderBusinessError(val errorMessageRegex: String) {
    EXTERNAL_REQUEST_FAILED("Request to ([A-Z]+) (.*) failed! sourceRefId: %s, statusCode: %s")
}
