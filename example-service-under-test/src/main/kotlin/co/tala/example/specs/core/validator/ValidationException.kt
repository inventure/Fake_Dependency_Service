package co.tala.example.specs.core.validator

import co.tala.example.http.client.lib.converter.InstantConverter
import co.tala.example.http.client.lib.converter.InstantConverterTimePrecision
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.time.Instant

class ValidationException(context: ValidationContext, exceptions: List<Exception>) :
    Exception(createMessage(context, exceptions)) {
    companion object {
        private val gson: Gson = GsonBuilder().registerTypeAdapter(
            Instant::class.java,
            InstantConverter(InstantConverterTimePrecision.PRECISION3)
        ).setPrettyPrinting().create()

        private fun createMessage(context: ValidationContext, exceptions: List<Exception>): String {
            val sb: StringBuilder = StringBuilder()
            sb.appendLine()
            sb.appendLine("Validations Failed!")
            sb.appendLine("Test Context:")
            sb.appendLine(gson.toJson(context))
            sb.appendLine()
            exceptions.forEachIndexed { index: Int, exception: Exception ->
                sb.appendLine("Assertion Failure ${index + 1}")
                sb.appendLine("${exception.message}")
                sb.appendLine()
            }
            return sb.toString()
        }
    }
}
