package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.Minecraft

class HeightPredicate: StaticPredicateType<HeightPredicate.Arguments>("height") {
    data class Arguments(val direction: Direction, val y: Int): TriggerArguments()
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::direction.name to "Whether the music should play when the player is above or below " +
                    "the y value.",
            Arguments::y.name to "Threshold at which the predicate should switch."
        )

    override fun validate(arguments: Arguments): Boolean {
        val minecraft = Minecraft.getInstance()
        val playerHeight = minecraft.player?.blockPosition()?.y ?: return false

        return if (arguments.direction == Direction.Above) playerHeight >= arguments.y else playerHeight <= arguments.y
    }

    @Suppress("unused")
    enum class Direction {
        Above,
        Below
    }
}