package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.identifier.EntityTypeIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient
import net.minecraft.util.JsonHelper

class EntityNearbyPredicate(private val entities: List<EntityTypeIdentifier>, private val blockRadius: UInt): MusicPredicate() {
    private val entityTranslationKeys = entities.map { entity -> entity.toTranslationKey("entity") }

    override fun test(client: MinecraftClient): Boolean {
        val playerEntity = client.player ?: return false
        val world = client.world ?: return false
        val validEntities =
            (if (entityTranslationKeys.isNotEmpty()) {
                world.entities.filter { entityTranslationKeys.any { key -> it.type.translationKey == key } }
            }
            else {
                world.entities
            })
                .filter { it != playerEntity }

        return validEntities.any { playerEntity.entityPos.distanceTo(it.entityPos).toUInt() <= blockRadius }
    }

    override fun getTickRate(): Int {
        return super.getTickRate() * 2
    }

    override fun toJson(): JsonObject {
        val result = JsonObject()
        val jsonEntities = JsonArray()
        entities.forEach { boss -> jsonEntities.add(boss.toString()) }
        result.add("entities", jsonEntities)
        result.addProperty("blockRadius", blockRadius.toInt())

        return result
    }

    companion object: MusicPredicateCompanion<EntityNearbyPredicate> {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                "entities" to "List of entities the music should play for. If none, any entity will trigger the music.",
                "blockRadius" to "Minimum radius for the entity to trigger the predicate."
            )

        override fun fromJson(json: JsonObject): EntityNearbyPredicate {
            return EntityNearbyPredicate(
                if (JsonHelper.hasArray(json, "entities"))
                    JsonHelper
                        .getArray(json, "entities")
                        .map { element -> EntityTypeIdentifier(element.asString) }
                else
                    listOf(EntityTypeIdentifier(JsonHelper.getString(json, "id"))),
                if (JsonHelper.hasNumber(json, "blockRadius"))
                    JsonHelper.getInt(json, "blockRadius").toUInt()
                else
                    0U
            )
        }
    }
}