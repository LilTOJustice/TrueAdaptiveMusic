package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient

class HungerPredicate(private val direction: Direction, private val hungerPercentage: Int): MusicPredicate() {
    override fun test(): Boolean {
        val client = MinecraftClient.getInstance()
        if (client.player?.isCreative == true || client.player?.isSpectator == true) {
            return false
        }

        val currentPercentage = (client.player?.hungerManager?.foodLevel ?: return false) / 20F
        val thresholdPercentage = hungerPercentage / 100F

        return when (direction) {
            Direction.Greater -> currentPercentage > thresholdPercentage
            Direction.Lesser -> currentPercentage < thresholdPercentage
        }
    }

    override fun getTickRate(): Int {
        return super.getTickRate() * 4
    }

    companion object: MusicPredicateCompanion {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                "direction" to "Whether the music should play when the player's hunger percentage is above or " +
                        "below the given percentage.",
                "hungerPercentage" to "Threshold at which the predicate should switch."
            )
    }

    enum class Direction {
        Greater,
        Lesser
    }
}