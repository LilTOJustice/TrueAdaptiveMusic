package liltojustice.trueadaptivemusic.client.serialization.legacy.original.model.trigger.predicate.types

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.serialization.legacy.Convertible
import liltojustice.trueadaptivemusic.client.serialization.legacy.original.model.identifier.Identifier

object StructureSetPredicate: Convertible {
    override fun convert(json: JsonElement): JsonObject {
        val result = JsonObject()
        val structureSets = JsonArray()
        json.asJsonObject
            .getAsJsonArray("id")
            ?.forEach { element -> structureSets.add(Identifier.convert(element)) }
        result.add("structureSets", structureSets)

        return result
    }
}