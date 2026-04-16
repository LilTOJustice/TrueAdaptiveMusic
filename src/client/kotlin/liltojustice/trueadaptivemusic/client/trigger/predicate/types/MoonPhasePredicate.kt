package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient

class MoonPhasePredicate(private val moonPhase: MoonPhase): MusicPredicate() {
    override fun test(): Boolean {
        val level = Minecraft.getInstance().level ?: return false
        val currentPhase = level.environmentAttributes.getDimensionValue(EnvironmentAttributes.MOON_PHASE)
        val time = level.overworldClockTime % 24000

        return time in 13000..23999 && when(moonPhase) {
            MoonPhase.Full -> currentPhase == 0
            MoonPhase.New -> currentPhase == 4
        }
    }

    override fun getTickRate(): Int {
        return super.getTickRate() * 10
    }

    companion object: MusicPredicateCompanion {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                MoonPhasePredicate::moonPhase.name to "What phase of the moon the music should play for.")
    }

    enum class MoonPhase {
        New,
        Full
    }
}