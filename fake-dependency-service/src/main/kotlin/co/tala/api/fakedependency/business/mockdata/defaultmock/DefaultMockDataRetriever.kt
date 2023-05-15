package co.tala.api.fakedependency.business.mockdata.defaultmock

import co.tala.api.fakedependency.configuration.defaultmock.DefaultCallbackConfiguration
import co.tala.api.fakedependency.constant.Constant
import co.tala.api.fakedependency.exception.MockNotFoundException
import co.tala.api.fakedependency.model.MockData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest
import kotlin.random.Random

@Component
class DefaultMockDataRetriever(
    private val defaultables: List<Defaultable>,
    private val config: DefaultCallbackConfiguration
) : IDefaultMockDataRetriever {

    override fun getDefaultMockData(request: HttpServletRequest, payload: Any?): MockData {
        val uri = request.requestURI.replace(Constant.MOCK_SERVICE, "")

        val defaultable: Defaultable = defaultables.firstOrNull {
            val regex = it.getUriRegex()
            uri matches regex
        } ?: throw MockNotFoundException()

        if (config.enabled) {
            val callback = defaultable.getCallback()
            if (callback.isEnabled)
                GlobalScope.launch {
                    delay(Random.nextLong(config.minimumDelay, config.maximumDelay))
                    callback.action.invoke(payload)
                }
        }

        return defaultable.getMockData(payload)
    }
}
