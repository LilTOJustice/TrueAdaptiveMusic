package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.Minecraft

class BossHealthPredicate: StaticPredicateType<BossHealthPredicate.Arguments>("boss_health") {
    override val tickRate
        get() = super.tickRate * 4
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::direction.name to "Whether the music should play above or below the given health percentage.",
            Arguments::healthPercentage.name to "The threshold at which the predicate switches."
        )

    override fun validatePredicate(arguments: Arguments): Boolean {
        return Minecraft.getInstance().gui.bossOverlay.events.any { bossBar ->
            healthTest(
                arguments.healthPercentage / 100F,
                arguments.direction,
                bossBar.value.progress
            )
        }
    }

    data class Arguments(val direction: Direction, val healthPercentage: Int): TriggerArguments()

    companion object {
        private fun healthTest(thresholdPercentage: Float, direction: Direction, currentPercentage: Float): Boolean {
            return when (direction) {
                Direction.Greater -> currentPercentage > thresholdPercentage
                Direction.Lesser -> currentPercentage < thresholdPercentage
            }
        }
    }

    enum class Direction {
        Greater,
        Lesser
    }
}