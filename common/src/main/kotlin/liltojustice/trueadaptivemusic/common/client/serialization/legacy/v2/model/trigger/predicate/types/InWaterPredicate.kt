package liltojustice.trueadaptivemusic.common.client.serialization.legacy.v2.model.trigger.predicate.types

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.common.client.serialization.legacy.Convertible
import net.minecraft.resources.Identifier

object InWaterPredicate: Convertible {
    override fun convert(json: JsonElement): JsonObject {
        val result = JsonObject()
        val fluidsArray = JsonArray()
        val fluidId = JsonObject()
        fluidId.add("id", Gson().toJsonTree(Identifier.parse("minecraft:water")))
        fluidsArray.add(fluidId)
        result.add("fluids", fluidsArray)

        return result
    }
}