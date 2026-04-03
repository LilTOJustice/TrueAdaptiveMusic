package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.identifier.EntityTypeIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft

class EntityNearbyPredicate(private val entities: List<EntityTypeIdentifier>, private val blockRadius: UInt): MusicPredicate() {
    private val entityTranslationKeys = entities.map { entity -> entity.toLanguageKey("entity") }

    override fun test(): Boolean {
        val minecraft = Minecraft.getInstance()
        val playerEntity = minecraft.player ?: return false
        val level = minecraft.level ?: return false
        val validEntities =
            (if (entityTranslationKeys.isNotEmpty()) {
                level.entitiesForRendering().filter { entityTranslationKeys.any { key -> it.type.descriptionId == key } }
            }
            else {
                level.entitiesForRendering()
            })
                .filter { it != playerEntity }

        return validEntities.any { playerEntity.position().distanceTo(it.position()).toUInt() <= blockRadius }
    }

    override fun getTickRate(): Int {
        return super.getTickRate() * 5
    }

    companion object: MusicPredicateCompanion {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                EntityNearbyPredicate::entities.name to "List of entities the music should play for. If none, any " +
                        "entity will trigger the music.",
                EntityNearbyPredicate::blockRadius.name to "Minimum radius for the entity to trigger the predicate."
            )
    }
}