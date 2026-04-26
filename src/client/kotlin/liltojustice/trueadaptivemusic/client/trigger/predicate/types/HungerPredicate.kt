package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.MinecraftClient
import net.minecraft.world.GameMode
import kotlin.reflect.typeOf

object HungerPredicate: StaticPredicateType<HungerPredicate.Arguments>(
    "hunger", typeOf<Arguments>()
) {
    override val tickRate: Int
        get() = super.tickRate * 4
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::direction.name to "Whether the music should play when the player's hunger percentage is above " +
                    "or below the given percentage.",
            Arguments::hungerPercentage.name to "Threshold at which the predicate should switch."
        )

    data class Arguments(val direction: Direction, val hungerPercentage: Int): TriggerArguments()

    override fun test(arguments: Arguments): Boolean {
        val minecraft = MinecraftClient.getInstance()
        if (minecraft.player?.gameMode in listOf(GameMode.CREATIVE, GameMode.SPECTATOR)) {
            return false
        }

        val currentPercentage = (minecraft.player?.hungerManager?.foodLevel ?: return false) / 20F
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