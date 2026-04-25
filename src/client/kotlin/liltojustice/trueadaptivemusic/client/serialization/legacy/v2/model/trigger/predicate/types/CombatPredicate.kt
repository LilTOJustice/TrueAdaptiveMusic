package liltojustice.trueadaptivemusic.client.serialization.legacy.v2.model.trigger.predicate.types

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import liltojustice.trueadaptivemusic.client.serialization.legacy.Convertible

object CombatPredicate: Convertible {
    override fun convert(json: JsonElement): JsonObject {
        val jsonObject = json.asJsonObject
        val result = JsonObject()
        val mobEntitiesArray = jsonObject.getAsJsonArray("mobEntities")
        result.add(
            "blacklist",
            jsonObject.getAsJsonPrimitive("blacklist") ?: JsonPrimitive(false)
        )
        result.add("entities", mobEntitiesArray)

        return result
    }
}