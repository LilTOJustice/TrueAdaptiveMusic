package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.identifier.EntityTypeIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient

class RidingPredicate(private val entities: List<EntityTypeIdentifier>): MusicPredicate() {
    override fun test(): Boolean {
        val client = MinecraftClient.getInstance()
        val vehicleKey = client.player?.vehicle?.type?.translationKey ?: return false

        return entities.isEmpty() || entities.any { entity -> entity.toTranslationKey("entity") == vehicleKey }
    }

    companion object: MusicPredicateCompanion {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                "entities" to "Which entities to ride for the music to play. If none, any entity will trigger the " +
                        "music."
            )
    }
}