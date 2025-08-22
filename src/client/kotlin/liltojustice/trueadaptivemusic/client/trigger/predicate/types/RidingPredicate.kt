package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.identifier.EntityTypeIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient
import net.minecraft.util.JsonHelper

class RidingPredicate(private val entities: List<EntityTypeIdentifier>): MusicPredicate() {
    override fun test(client: MinecraftClient): Boolean {
        val vehicleKey = client.player?.vehicle?.type?.translationKey ?: return false

        return entities.isEmpty() || entities.any { entity -> entity.toTranslationKey("entity") == vehicleKey }
    }

    override fun toJson(): JsonObject {
        val result = JsonObject()
        val jsonEntities = JsonArray()
        entities.forEach { entity -> jsonEntities.add(entity.toString()) }
        result.add("entities", jsonEntities)

        return result
    }

    companion object: MusicPredicateCompanion<RidingPredicate> {
        override fun fromJson(json: JsonObject): RidingPredicate {
            return RidingPredicate(
                JsonHelper.getArray(json, "entities")
                    .map { element -> EntityTypeIdentifier(element.asString) })
        }
    }
}