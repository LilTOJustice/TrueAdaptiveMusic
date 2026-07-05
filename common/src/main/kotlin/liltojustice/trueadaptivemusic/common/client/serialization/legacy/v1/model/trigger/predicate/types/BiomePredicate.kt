package liltojustice.trueadaptivemusic.common.client.serialization.legacy.v1.model.trigger.predicate.types

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.common.client.serialization.legacy.Convertible
import liltojustice.trueadaptivemusic.common.client.serialization.legacy.v1.model.identifier.Identifier

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