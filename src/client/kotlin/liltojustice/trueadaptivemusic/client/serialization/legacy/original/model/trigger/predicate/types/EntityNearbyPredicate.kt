package liltojustice.trueadaptivemusic.client.serialization.legacy.original.model.trigger.predicate.types

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.serialization.legacy.Convertible
import liltojustice.trueadaptivemusic.client.serialization.legacy.original.model.identifier.Identifier

object EntityNearbyPredicate: Convertible {
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