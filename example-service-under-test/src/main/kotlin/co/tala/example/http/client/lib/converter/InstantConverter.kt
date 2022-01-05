package co.tala.example.http.client.lib.converter


import com.google.gson.*
import java.lang.reflect.Type
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val zoneId = ZoneId.of("UTC")
private val SERIALIZE_FORMATTER_PRECISION3 = DateTimeFormatter
    .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    .withZone(zoneId)
private val SERIALIZE_FORMATTER_PRECISION6 = DateTimeFormatter
    .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
    .withZone(zoneId)
private val DESERIALIZE_FORMATTER = DateTimeFormatter.ISO_INSTANT

class InstantConverter(
    private val precision: InstantConverterTimePrecision
) : JsonSerializer<Instant>, JsonDeserializer<Instant> {
    override fun serialize(src: Instant?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement =
        JsonPrimitive((when (precision) {
            InstantConverterTimePrecision.PRECISION6 -> SERIALIZE_FORMATTER_PRECISION6
            InstantConverterTimePrecision.PRECISION3 -> SERIALIZE_FORMATTER_PRECISION3
        }).format(src))

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Instant? =
        if (json != null) DESERIALIZE_FORMATTER.parse(json.asString, Instant::from) else null
}

enum class InstantConverterTimePrecision {
    PRECISION6,
    PRECISION3
}
