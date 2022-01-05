package co.tala.example.api.immunization_decider.controller.model

data class InitiateRequestDto(
    val sourceRefId: String,
    val userId: Long
)
