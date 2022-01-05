package co.tala.api.fakedependency.model


data class ResponseSetUpMetadata(
    val httpStatus: Int = 200,
    val delayMs: Long = 0
)
