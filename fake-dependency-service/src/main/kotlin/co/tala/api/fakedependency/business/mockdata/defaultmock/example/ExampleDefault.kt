package co.tala.api.fakedependency.business.mockdata.defaultmock.example

import co.tala.api.fakedependency.business.mockdata.defaultmock.Defaultable
import co.tala.api.fakedependency.business.mockdata.defaultmock.DefaultCallback
import co.tala.api.fakedependency.model.MockData
import co.tala.api.fakedependency.redis.IRedisService
import kotlin.random.Random


class ExampleDefault(private val redisSvc: IRedisService) : Defaultable {
    /**
     * An example of a default mock data being returned
     */
    override fun getMockData(payload: Any?): MockData = mockData(
        responseBody = mapOf(
            "someExampleProperty" to Random.nextLong().toString(),
            "somePayloadCaptureExample" to payload
        ),
        httpStatus = 202
    )

    /**
     * If the request uri matches this regex, then this [Defaultable] will be invoked on.
     */
    override fun getUriRegex(): Regex = Regex("/regexes/([0-9]*)/examples")

    /**
     * A callback can do any action after the mock data is retrieved. This can be used to call back a service. In this
     * example, it just stored the payload in redis.
     */
    override fun getCallback(): DefaultCallback = DefaultCallback(
        isEnabled = true,
        action = { payload: Any? ->
            if (payload != null)
                redisSvc.setValue("defaultCallbackExample", Random.nextLong().toString(), payload)
        }
    )
}
