package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.Minecraft

class HealthPredicate: StaticPredicateType<HealthPredicate.Arguments>("health") {
    data class Arguments(val healthType: HealthType, val direction: Direction, val health: Int)
        : TriggerArguments()
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::healthType.name to "Whether the health setting is a value or percentage.",
            Arguments::direction.name to "Whether the music should play above or below the health setting.",
            Arguments::health.name to "Threshold at which the predicate should switch."
        )

    override fun validate(arguments: Arguments): Boolean {
        val minecraft = Minecraft.getInstance()
        val player = minecraft.player ?: return false
        val typeAdjusted = if (arguments.healthType == HealthType.Percentage)
            player.maxHealth * (arguments.health / 100F)
        else
            arguments.health.toFloat()

        return when (arguments.direction) {
            Direction.Greater -> player.health > typeAdjusted
            Direction.GreaterOrEqual -> player.health >= typeAdjusted
            Direction.Lesser -> player.health < typeAdjusted
            Direction.LesserOrEqual -> player.health <= typeAdjusted
        }
    }

    @Suppress("UNUSED")
    enum class HealthType {
        Value,
        Percentage
    }

    enum class Direction {
        Greater,
        GreaterOrEqual,
        Lesser,
        LesserOrEqual
    }
}