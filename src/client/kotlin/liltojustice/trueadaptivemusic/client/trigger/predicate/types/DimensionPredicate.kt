package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.identifier.DimensionIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft

class DimensionPredicate(private val dimensions: List<DimensionIdentifier>): MusicPredicate() {
    override fun test(): Boolean {
        val playerDimension = Minecraft.getInstance().player?.level()?.dimensionTypeRegistration() ?: return false

        return dimensions.isEmpty() || dimensions.any { dimension -> playerDimension.`is`((dimension.id)) }
    }

    companion object: MusicPredicateCompanion {
        override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            DimensionPredicate::dimensions.name to "Select all dimensions the music should play for. If none, any " +
                    "dimension will trigger the music."
        )
    }
}