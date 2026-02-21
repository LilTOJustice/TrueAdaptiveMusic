package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient

class HeightPredicate(private val above: Boolean, private val y: Int): MusicPredicate() {
    override fun test(): Boolean {
        val client = MinecraftClient.getInstance()
        val playerHeight = client.player?.blockPos?.y ?: return false

        return if (above) playerHeight >= y else playerHeight <= y
    }

    companion object: MusicPredicateCompanion {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                "above" to "Whether the music should play when the player is above or below the y value.",
                "y" to "Threshold at which the predicate should switch."
            )
    }
}