package liltojustice.trueadaptivemusic.client.serialization.legacy.original.model.trigger.event

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.serialization.legacy.Convertible
import liltojustice.trueadaptivemusic.client.serialization.legacy.original.model.trigger.event.types.OnBossDefeatEvent
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent

object MusicEvent: Convertible {
    override fun convert(json: JsonElement): JsonObject {
        val jsonObject = json.asJsonObject
        val result = JsonObject()

        val type = jsonObject.getAsJsonPrimitive("type").asString
        result.addProperty("type", type)

        result.add("music", jsonObject.getAsJsonArray("musicPath") ?: JsonArray())
        result.add(
            "parameters",
            jsonObject.get("parameters") ?: Gson().toJsonTree(MusicEvent.Parameters.default())
        )

        val rest = convertFor(type, jsonObject).entrySet()
        rest.forEach { entry -> result.add(entry.key, entry.value) }

        return result
    }

    private fun convertFor(type: String, json: JsonObject): JsonObject {
        return getConvertibleFor(type)?.convert(json) ?: json
    }

    private fun getConvertibleFor(type: String): Convertible? {
        return when(type) {
            "on_boss_defeat" -> OnBossDefeatEvent
            else -> null
        }
    }
}