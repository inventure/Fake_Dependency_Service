package co.tala.api.fakedependency.business.mockdata.defaultmock.braze

import co.tala.api.fakedependency.business.mockdata.defaultmock.Defaultable
import co.tala.api.fakedependency.model.MockData

class BrazeDataDefault : Defaultable {
    override fun getMockData(payload: Any?): MockData = mockData(
        responseBody = mapOf(
            "api_key" to "fake-api-key",
            "app_version" to "26.14.0.dev",
            "app_version_code" to "1",
            "sdk_version" to "43.0.0",
            "device_id" to "fake-device-id",
            "time" to (System.currentTimeMillis() / 1000),
            "device" to mapOf(
                "brand" to "samsung",
                "model" to "SM-A235F",
                "os_version" to "14",
                "carrier" to "fake-carrier",
                "locale" to "es_MX",
                "time_zone" to "America/Mexico_City"
            ),
            "events" to listOf(
                mapOf(
                    "name" to "users.track",
                    "user_id" to (payload as? Map<*, *>)?.get("user_id") ?: "fake-user-id",
                    "time" to (System.currentTimeMillis() / 1000.0),
                    "session_id" to "fake-session-id"
                )
            ),
            "attributes" to listOf(
                mapOf(
                    "user_id" to (payload as? Map<*, *>)?.get("user_id") ?: "fake-user-id"
                )
            )
        ),
        httpStatus = 200,
        responseHeaders = emptyMap()
    )

    override fun getUriRegex(): Regex = Regex("/braze/api/v3/data.*")
}

class BrazeContentCardsSyncDefault : Defaultable {
    override fun getMockData(payload: Any?): MockData = mockData(
        responseBody = mapOf(
            "api_key" to "fake-api-key",
            "sdk_version" to "43.0.0",
            "device_id" to "fake-device-id",
            "time" to (System.currentTimeMillis() / 1000),
            "user_id" to (payload as? Map<*, *>)?.get("user_id") ?: "fake-user-id",
            "cards" to emptyList<Any>(),
            "full_sync" to true
        ),
        httpStatus = 200,
        responseHeaders = emptyMap()
    )

    override fun getUriRegex(): Regex = Regex("/braze/api/v3/content_cards/sync.*")
}
