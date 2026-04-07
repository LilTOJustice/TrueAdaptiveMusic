package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft
import net.minecraft.world.level.GameType

class HungerPredicate(private val direction: Direction, private val hungerPercentage: Int): MusicPredicate() {
    override fun test(): Boolean {
        val minecraft = Minecraft.getInstance()
        if (minecraft.player?.gameMode() in listOf(GameType.CREATIVE, GameType.SPECTATOR)) {
            return false
        }

        val currentPercentage = (minecraft.player?.foodData?.foodLevel ?: return false) / 20F
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
                HungerPredicate::direction.name to "Whether the music should play when the player's hunger " +
                        "percentage is above or below the given percentage.",
                HungerPredicate::hungerPercentage.name to "Threshold at which the predicate should switch."
            )
    }

    enum class Direction {
        Greater,
        Lesser
    }
}