package liltojustice.trueadaptivemusic.client.serialization.legacy.v1.model.trigger.predicate.types

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.serialization.legacy.Convertible
import liltojustice.trueadaptivemusic.client.serialization.legacy.v1.model.identifier.Identifier

object RidingPredicate: Convertible {
    override fun convert(json: JsonElement): JsonObject {
        val result = JsonObject()
        val entities = JsonArray()
        json.asJsonObject
            .getAsJsonArray("entities")
            ?.forEach { element -> entities.add(Identifier.convert(element)) }
        result.add("entities", entities)

        return result
    }
}