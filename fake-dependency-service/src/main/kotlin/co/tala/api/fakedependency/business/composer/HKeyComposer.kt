package co.tala.api.fakedependency.business.composer

import co.tala.api.fakedependency.business.helper.IKeyHelper
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

@Component
class HKeyComposer(
    private val keyHelper: IKeyHelper
) : IHKeyComposer {
    /**
     * Gets hash keys both with and without id
     */
    override fun getKeys(requestId: String?, request: HttpServletRequest): List<String> = listOf(
        getHKey(requestId, request),
        getHKey(null, request)
    ).distinct()

    /**
     * Concatenates the X-Request-ID to the request uri
     */
    private fun getHKey(id: String?, request: HttpServletRequest): String {
        val uri = request.requestURI.replaceFirst(Regex("/(mock-resources)"), "")
        val keys = listOfNotNull(id, uri).toTypedArray()
        return keyHelper.concatenateKeys(*keys)
    }
}
