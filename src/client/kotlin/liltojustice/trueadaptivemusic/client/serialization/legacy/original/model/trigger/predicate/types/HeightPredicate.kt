package liltojustice.trueadaptivemusic.client.serialization.legacy.original.model.trigger.predicate.types

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.serialization.legacy.Convertible

object HeightPredicate: Convertible {
    override fun convert(json: JsonElement): JsonObject {
        val jsonObject = json.asJsonObject
        val result = JsonObject()
        val above = jsonObject.getAsJsonPrimitive("direction").asBoolean
        val direction = if (above) "Above" else "Below"

        result.addProperty("direction", direction)
        result.add("y", jsonObject.getAsJsonPrimitive("y"))

        return result
    }
}