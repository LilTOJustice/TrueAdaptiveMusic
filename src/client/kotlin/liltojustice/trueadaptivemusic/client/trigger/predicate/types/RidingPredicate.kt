package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.identifier.EntityTypeIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft

class RidingPredicate(private val entities: List<EntityTypeIdentifier>): MusicPredicate() {
    override fun test(): Boolean {
        val vehicleKey = Minecraft.getInstance().player?.vehicle?.type?.descriptionId ?: return false

        return entities.isEmpty() || entities.any { entity -> entity.toLanguageKey("entity") == vehicleKey }
    }

    companion object: MusicPredicateCompanion {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                RidingPredicate::entities.name to "Which entities to ride for the music to play. If none, any entity " +
                        "will trigger the music."
            )
    }
}