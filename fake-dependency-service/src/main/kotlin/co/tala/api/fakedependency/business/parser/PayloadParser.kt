package co.tala.api.fakedependency.business.parser

import co.tala.api.fakedependency.exception.PayloadTypeNotSupportedException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class PayloadParser(
    private val mapper: ObjectMapper,
    private val xmlMapper: XmlMapper
) : IPayloadParser {

    override fun parse(payload: Any, key: String): String? = findValue(payload, key.split('.'), 0)

    @Suppress("UNCHECKED_CAST")
    private fun findValue(any: Any, keys: List<String>, depth: Int): String? = when {
        keys.size == depth -> any.toString()
        any is Map<*, *> -> findValueInMap(any as Map<String, Any>, keys, depth)
        any is String -> {
            findValueInXmlString(any.toString(), keys, depth) ?: if (keys.size == 1) {
                findValueInUrlEncodedForm(any.toString(), keys.first())
            } else null
        }

        else -> throw PayloadTypeNotSupportedException(any)
    }

    private fun findValueInXmlString(xml: String, keys: List<String>, depth: Int): String? {
        val any = try {
            xmlMapper.readValue(xml, Any::class.java)
        } catch (ignored: IOException) {
            return null
        }
        val map = mapper.convertValue(any, object : TypeReference<Map<String, Any>>() {})
        return findValueInMap(map, keys, depth)
    }

    private fun findValueInMap(map: Map<String, Any>, keys: List<String>, depth: Int): String? {
        val key = keys[depth]
        val child = map.getOrElse(key) { return null }
        return findValue(child, keys, depth + 1)
    }

    private fun findValueInUrlEncodedForm(form: String, key: String): String? {
        return form.split("&").asSequence()
            .map { it.split("=") }
            .filter { it.size == 2 }
            .map { it[0] to it[1] }
            .filter { it.first.isNotEmpty() && it.second.isNotEmpty() }
            .find { it.first == key }
            ?.second
    }
}
