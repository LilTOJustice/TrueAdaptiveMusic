package liltojustice.trueadaptivemusic.common.client.serialization.legacy.v1.model.identifier

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.common.client.serialization.legacy.Convertible
import net.minecraft.resources.Identifier

object Identifier: Convertible {
    override fun convert(json: JsonElement): JsonObject {
        val result = JsonObject()
        val id = Gson().toJsonTree(Identifier.parse(json.asString))
        result.add("id", id)

        return result
    }
}