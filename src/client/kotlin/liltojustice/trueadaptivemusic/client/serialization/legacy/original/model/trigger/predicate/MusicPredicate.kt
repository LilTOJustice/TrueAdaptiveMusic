package liltojustice.trueadaptivemusic.client.serialization.legacy.original.model.trigger.predicate

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.serialization.legacy.Convertible
import liltojustice.trueadaptivemusic.client.serialization.legacy.original.model.trigger.predicate.types.BiomePredicate
import liltojustice.trueadaptivemusic.client.serialization.legacy.original.model.trigger.predicate.types.BossPredicate
import liltojustice.trueadaptivemusic.client.serialization.legacy.original.model.trigger.predicate.types.CombatPredicate
import liltojustice.trueadaptivemusic.client.serialization.legacy.original.model.trigger.predicate.types.DimensionPredicate
import liltojustice.trueadaptivemusic.client.serialization.legacy.original.model.trigger.predicate.types.EntityNearbyPredicate
import liltojustice.trueadaptivemusic.client.serialization.legacy.original.model.trigger.predicate.types.HeightPredicate
import liltojustice.trueadaptivemusic.client.serialization.legacy.original.model.trigger.predicate.types.RidingPredicate
import liltojustice.trueadaptivemusic.client.serialization.legacy.original.model.trigger.predicate.types.StatusEffectPredicate
import liltojustice.trueadaptivemusic.client.serialization.legacy.original.model.trigger.predicate.types.StructurePredicate
import liltojustice.trueadaptivemusic.client.serialization.legacy.original.model.trigger.predicate.types.StructureSetPredicate

object MusicPredicate: Convertible {
    override fun convert(json: JsonElement): JsonObject {
        val jsonObject = json.asJsonObject
        val result = JsonObject()

        val type = jsonObject.getAsJsonPrimitive("type")?.asString ?: "ErrorPredicate"
        result.addProperty("type", type)

        val rest = convertFor(type, jsonObject).entrySet()
        rest.forEach { entry -> result.add(entry.key, entry.value) }

        return result
    }

    private fun convertFor(type: String, json: JsonObject): JsonObject {
        return getConvertibleFor(type)?.convert(json) ?: json
    }

    private fun getConvertibleFor(type: String): Convertible? {
        return when(type) {
            "biome" -> BiomePredicate
            "boss" -> BossPredicate
            "combat" -> CombatPredicate
            "dimension" -> DimensionPredicate
            "entity_nearby" -> EntityNearbyPredicate
            "height" -> HeightPredicate
            "riding" -> RidingPredicate
            "status_effect" -> StatusEffectPredicate
            "structure" -> StructurePredicate
            "structure_set" -> StructureSetPredicate
            else -> null
        }
    }
}