package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.Minecraft
import net.minecraft.world.attribute.EnvironmentAttributes

class MoonPhasePredicate: StaticPredicateType<MoonPhasePredicate.Arguments>("moon_phase") {
    override val tickRate: Int
        get() = super.tickRate * 10

    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::moonPhase.name to "What phase of the moon the music should play for.")

    data class Arguments(val moonPhase: MoonPhase): TriggerArguments()

    override fun validate(arguments: Arguments): Boolean {
        val level = Minecraft.getInstance().level ?: return false
        val currentPhase = level.environmentAttributes.getDimensionValue(EnvironmentAttributes.MOON_PHASE)
        val time = level.overworldClockTime % 24000

        return time in 13000..23999 && when(arguments.moonPhase) {
            MoonPhase.Full -> currentPhase == net.minecraft.world.level.MoonPhase.FULL_MOON
            MoonPhase.New -> currentPhase == net.minecraft.world.level.MoonPhase.NEW_MOON
        }
    }

    enum class MoonPhase {
        New,
        Full
    }
}