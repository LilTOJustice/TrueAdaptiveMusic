package liltojustice.trueadaptivemusic.client.serialization.legacy.v2.model.trigger.predicate

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.serialization.legacy.Convertible
import liltojustice.trueadaptivemusic.client.serialization.legacy.v2.model.trigger.predicate.types.CombatPredicate
import liltojustice.trueadaptivemusic.client.serialization.legacy.v2.model.trigger.predicate.types.InLavaPredicate
import liltojustice.trueadaptivemusic.client.serialization.legacy.v2.model.trigger.predicate.types.InWaterPredicate

object MusicPredicate: Convertible {
    override fun convert(json: JsonElement): JsonObject {
        val jsonObject = json.asJsonObject
        val result = JsonObject()

        val type = jsonObject.getAsJsonPrimitive("type")?.asString ?: "ErrorPredicate"
        result.addProperty("type", convertTypeName(type))

        val argumentsJson = jsonObject.deepCopy()
        argumentsJson.remove("type")

        result.add("arguments", convertArguments(type, argumentsJson))

        return result
    }

    private fun convertArguments(type: String, json: JsonObject): JsonObject {
        return getArgumentsFor(type)?.convert(json) ?: json
    }

    private fun convertTypeName(typeName: String): String {
        return when(typeName) {
            "in_lava" -> "in_fluid"
            "in_water" -> "in_fluid"
            else -> typeName
        }
    }

    private fun getArgumentsFor(type: String): Convertible? {
        return when(type) {
            "in_lava" -> InLavaPredicate
            "in_water" -> InWaterPredicate
            "combat" -> CombatPredicate
            else -> null
        }
    }
}