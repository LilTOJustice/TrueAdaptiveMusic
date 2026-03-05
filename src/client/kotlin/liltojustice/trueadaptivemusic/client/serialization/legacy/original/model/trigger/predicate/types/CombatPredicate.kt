package liltojustice.trueadaptivemusic.client.serialization.legacy.original.model.trigger.predicate.types

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import liltojustice.trueadaptivemusic.client.serialization.legacy.Convertible
import liltojustice.trueadaptivemusic.client.serialization.legacy.original.model.identifier.Identifier

object CombatPredicate: Convertible {
    override fun convert(json: JsonElement): JsonObject {
        val jsonObject = json.asJsonObject
        val result = JsonObject()
        val mobEntitiesArray = JsonArray()
        jsonObject
            .getAsJsonArray("id")
            ?.forEach { element -> mobEntitiesArray.add(Identifier.convert(element)) }
        result.add(
            "blacklist",
            jsonObject.getAsJsonPrimitive("blacklist") ?: JsonPrimitive(false)
        )
        result.add("mobEntities", mobEntitiesArray)

        return result
    }
}