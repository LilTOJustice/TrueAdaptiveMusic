package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.MinecraftClient
import kotlin.reflect.typeOf

object MoonPhasePredicate: StaticPredicateType<MoonPhasePredicate.Arguments>(
    "moon_phase", typeOf<Arguments>()
) {
    override val tickRate: Int
        get() = super.tickRate * 10

    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::moonPhase.name to "What phase of the moon the music should play for.")

    data class Arguments(val moonPhase: MoonPhase): TriggerArguments()

    override fun test(arguments: Arguments): Boolean {
        val level = MinecraftClient.getInstance().world ?: return false
        val currentPhase = level.moonPhase
        val time = level.timeOfDay % 24000

        return time in 13000..23999 && when(arguments.moonPhase) {
            MoonPhase.Full -> currentPhase == 0
            MoonPhase.New -> currentPhase == 4
        }
    }

    enum class MoonPhase {
        New,
        Full
    }
}