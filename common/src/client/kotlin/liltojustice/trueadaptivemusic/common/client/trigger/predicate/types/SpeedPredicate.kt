package liltojustice.trueadaptivemusic.common.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.Minecraft
import net.minecraft.world.phys.Vec3
import kotlin.reflect.typeOf

object SpeedPredicate: StaticPredicateType<SpeedPredicate.Arguments>(
    "speed", typeOf<Arguments>()
) {
    data class Arguments(val comparison: Comparison, val speedType: SpeedType, val speed: UInt)
        : TriggerArguments()
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::comparison.name to "Whether the music should play above or below the health setting.",
            Arguments::speedType.name to "Select whether all speed, horizontal speed, or vertical speed is used.",
            Arguments::speed.name to "Speed threshold in blocks/second at which the predicate should switch."
        )

    override fun test(arguments: Arguments): Boolean {
        val player = Minecraft.getInstance().player ?: return false
        val speedSqr = arguments.speed.toDouble().let { it * it }
        val tickRate = player.level().tickRateManager().tickrate().toDouble()
        val currentSpeedSqr = when (arguments.speedType) {
            SpeedType.All -> player.deltaMovement.scale(tickRate).distanceToSqr(Vec3.ZERO)
            SpeedType.Horizontal -> player.deltaMovement.horizontal().scale(tickRate).distanceToSqr(Vec3.ZERO)
            SpeedType.Vertical -> player.deltaMovement.y * player.deltaMovement.y * tickRate
        }

        return when (arguments.comparison) {
            Comparison.Greater -> currentSpeedSqr > speedSqr
            Comparison.GreaterOrEqual -> currentSpeedSqr >= speedSqr
            Comparison.Lesser -> currentSpeedSqr < speedSqr
            Comparison.LesserOrEqual -> currentSpeedSqr <= speedSqr
        }
    }

    enum class SpeedType {
        All,
        Horizontal,
        Vertical
    }

    enum class Comparison {
        Greater,
        GreaterOrEqual,
        Lesser,
        LesserOrEqual
    }
}