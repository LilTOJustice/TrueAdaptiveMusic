package liltojustice.trueadaptivemusic.client.serialization.legacy.original.model.trigger.predicate.types

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.serialization.legacy.Convertible

object WeatherPredicate: Convertible {
    override fun convert(json: JsonElement): JsonObject {
        val result = JsonObject()
        result.add("weather", json.asJsonObject.getAsJsonPrimitive("weatherType"))

        return result
    }
}