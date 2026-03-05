package liltojustice.trueadaptivemusic.client.serialization.legacy.original.model.trigger.predicate.types

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import liltojustice.trueadaptivemusic.client.serialization.legacy.Convertible

object HeightPredicate: Convertible {
    override fun convert(json: JsonElement): JsonObject {
        val jsonObject = json.asJsonObject
        val result = JsonObject()
        val above = jsonObject.getAsJsonPrimitive("above")?.asBoolean ?: false
        val direction = if (above) "Above" else "Below"

        result.addProperty("direction", direction)
        result.add("y", jsonObject.getAsJsonPrimitive("y") ?: JsonPrimitive(0))

        return result
    }
}