package co.tala.api.fakedependency.exception

class PayloadTypeNotSupportedException(any: Any) : Exception(generateMessage(any)) {
    private companion object {
        fun generateMessage(any: Any) =
            "Type of ${any.javaClass} passed is not supported! Please pass either an XML String or Map"
    }
}
