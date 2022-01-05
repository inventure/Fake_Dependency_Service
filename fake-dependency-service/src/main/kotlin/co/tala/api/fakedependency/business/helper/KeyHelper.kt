package co.tala.api.fakedependency.business.helper

import org.springframework.stereotype.Component

@Component
class KeyHelper : IKeyHelper {
    override fun concatenateKeys(vararg keys: Any): String = keys.toList().joinToString("-")
}
