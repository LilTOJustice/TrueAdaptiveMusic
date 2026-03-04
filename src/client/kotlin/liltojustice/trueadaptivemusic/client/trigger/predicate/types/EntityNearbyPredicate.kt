package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.identifier.EntityTypeIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient

class EntityNearbyPredicate(private val entities: List<EntityTypeIdentifier>, private val blockRadius: UInt): MusicPredicate() {
    private val entityTranslationKeys = entities.map { entity -> entity.toTranslationKey("entity") }

    override fun test(): Boolean {
        val client = MinecraftClient.getInstance()
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

        return validEntities.any { playerEntity.pos.distanceTo(it.pos).toUInt() <= blockRadius }
    }

    override fun getTickRate(): Int {
        return super.getTickRate() * 2
    }

    companion object: MusicPredicateCompanion {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                "entities" to "List of entities the music should play for. If none, any entity will trigger the music.",
                "blockRadius" to "Minimum radius for the entity to trigger the predicate."
            )
    }
}