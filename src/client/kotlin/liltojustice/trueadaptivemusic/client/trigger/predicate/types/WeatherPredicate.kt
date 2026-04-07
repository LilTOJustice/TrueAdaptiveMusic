package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft

class WeatherPredicate(private val weather: Weather): MusicPredicate() {
    override fun test(): Boolean {
        val minecraft = Minecraft.getInstance()
        val level = minecraft.level ?: return false

        return when(weather) {
            Weather.Clear -> !level.isRaining
            Weather.Rain -> level.isRaining
            Weather.Thunder -> level.isThundering
        }
    }

    override fun getTickRate(): Int {
        return super.getTickRate() * 3
    }

    companion object: MusicPredicateCompanion {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                WeatherPredicate::weather.name to "Which weather the music should play for.")
    }

    enum class Weather {
        Clear,
        Rain,
        Thunder
    }
}