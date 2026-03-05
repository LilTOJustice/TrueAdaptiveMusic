package liltojustice.trueadaptivemusic.client.serialization.legacy.original.model.trigger.predicate.types

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.serialization.legacy.Convertible
import liltojustice.trueadaptivemusic.client.serialization.legacy.original.model.identifier.Identifier

object BiomePredicate: Convertible {
    override fun convert(json: JsonElement): JsonObject {
        val result = JsonObject()
        val biomesArray = JsonArray()
        json.asJsonObject
            .getAsJsonArray("id")
            ?.forEach { element -> biomesArray.add(Identifier.convert(element)) }
        result.add("biomes", biomesArray)

        return result
    }
}