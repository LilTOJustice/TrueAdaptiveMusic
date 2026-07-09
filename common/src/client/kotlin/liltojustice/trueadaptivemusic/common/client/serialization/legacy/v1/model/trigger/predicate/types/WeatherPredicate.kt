package liltojustice.trueadaptivemusic.common.client.serialization.legacy.v1.model.trigger.predicate.types

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.common.client.serialization.legacy.Convertible

object WeatherPredicate: Convertible {
    override fun convert(json: JsonElement): JsonObject {
        val result = JsonObject()
        result.add("weather", json.asJsonObject.getAsJsonPrimitive("weatherType"))

        return result
    }
}