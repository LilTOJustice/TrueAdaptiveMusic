package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient

class HeightPredicate(private val direction: Direction, private val y: Int): MusicPredicate() {
    override fun test(): Boolean {
        val client = MinecraftClient.getInstance()
        val playerHeight = client.player?.blockPos?.y ?: return false

        return if (direction == Direction.Above) playerHeight >= y else playerHeight <= y
    }

    companion object: MusicPredicateCompanion {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                "direction" to "Whether the music should play when the player is above or below the y value.",
                "y" to "Threshold at which the predicate should switch."
            )
    }

    @Suppress("unused")
    enum class Direction {
        Above,
        Below
    }
}