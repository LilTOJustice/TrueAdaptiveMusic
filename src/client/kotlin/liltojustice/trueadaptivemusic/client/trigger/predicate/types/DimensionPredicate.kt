package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.identifier.DimensionIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient

class DimensionPredicate(private val dimensions: List<DimensionIdentifier>): MusicPredicate() {
    override fun test(): Boolean {
        val client = MinecraftClient.getInstance()
        val playerDimension = client.player?.world?.dimensionEntry ?: return false

        return dimensions.isEmpty() ||
                dimensions.any { dimension -> playerDimension.matchesId(dimension.id) }
    }

    companion object: MusicPredicateCompanion {
        override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            "dimensions" to "Select all dimensions the music should play for. If none, any dimension will trigger " +
                    "the music."
        )
    }
}