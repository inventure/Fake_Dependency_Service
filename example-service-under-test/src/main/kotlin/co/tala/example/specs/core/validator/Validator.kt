package co.tala.example.specs.core.validator

class Validator(private val context: ValidationContext) {
    private val exceptions: MutableList<Exception> = mutableListOf()

    fun assert(block: () -> Unit) = assert(null, block)

    fun assert(hint: String?, block: () -> Unit) = apply {
        try {
            block()
        } catch (ex: Throwable) {
            val message = "${if (hint != null) "Assertion failed! Hint: '$hint'" else ""}\n${ex.message}"
            exceptions.add(Exception(message))
        }
    }

    fun validateAll(validations: Validator.() -> Validator): Boolean = validations(this).throwExceptionsIfAny()

    fun <T> validateAll(validations: Validator.() -> T): T = validations(this).also { throwExceptionsIfAny() }

    private fun throwExceptionsIfAny(): Boolean {
        if (exceptions.isNotEmpty()) {
            val exception = ValidationException(context, exceptions)
            exceptions.clear()
            throw exception
        }
        return true
    }
}
