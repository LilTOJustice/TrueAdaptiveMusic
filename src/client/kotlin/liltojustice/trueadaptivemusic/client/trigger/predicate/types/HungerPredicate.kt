package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.Minecraft
import net.minecraft.world.level.GameType

class HungerPredicate: StaticPredicateType<HungerPredicate.Arguments>("hunger") {
    override val tickRate: Int
        get() = super.tickRate * 4
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::direction.name to "Whether the music should play when the player's hunger percentage is above " +
                    "or below the given percentage.",
            Arguments::hungerPercentage.name to "Threshold at which the predicate should switch."
        )

    data class Arguments(val direction: Direction, val hungerPercentage: Int): TriggerArguments()

    override fun validate(arguments: Arguments): Boolean {
        val minecraft = Minecraft.getInstance()
        if (minecraft.player?.gameMode() in listOf(GameType.CREATIVE, GameType.SPECTATOR)) {
            return false
        }

        val currentPercentage = (minecraft.player?.foodData?.foodLevel ?: return false) / 20F
        val thresholdPercentage = arguments.hungerPercentage / 100F

        return when (arguments.direction) {
            Direction.Greater -> currentPercentage > thresholdPercentage
            Direction.Lesser -> currentPercentage < thresholdPercentage
        }
    }

    enum class Direction {
        Greater,
        Lesser
    }
}