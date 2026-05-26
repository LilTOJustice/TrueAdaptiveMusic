package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.MinecraftClient
import kotlin.reflect.typeOf

object SpeedPredicate: StaticPredicateType<SpeedPredicate.Arguments>(
    "speed", typeOf<Arguments>()
) {
    private const val TICK_RATE = 20

    data class Arguments(val comparison: Comparison, val speedType: SpeedType, val speed: UInt)
        : TriggerArguments()
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::comparison.name to "Whether the music should play above or below the health setting.",
            Arguments::speedType.name to "Select whether all speed, horizontal speed, or vertical speed is used." +
                    "\n**Has no affect in 1.20.1, only horizontal works!**",
            Arguments::speed.name to "Speed threshold in blocks/second at which the predicate should switch."
        )

    override fun test(arguments: Arguments): Boolean {
        val player = MinecraftClient.getInstance().player ?: return false
        val speed = (player.horizontalSpeed - player.prevHorizontalSpeed) * TICK_RATE
        val argSpeed = arguments.speed.toDouble()

        return when (arguments.comparison) {
            Comparison.Greater -> speed > argSpeed
            Comparison.GreaterOrEqual -> speed >= argSpeed
            Comparison.Lesser -> speed < argSpeed
            Comparison.LesserOrEqual -> speed <= argSpeed
        }
    }

    @Suppress("UNUSED")
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