package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient

class WeatherPredicate(private val weather: Weather): MusicPredicate() {
    override fun test(): Boolean {
        val client = MinecraftClient.getInstance()
        val properties = client.world?.levelProperties ?: return false

        return when(weather) {
            Weather.Clear -> !properties.isRaining
            Weather.Rain -> properties.isRaining
            Weather.Thunder -> properties.isThundering
        }
    }

    override fun getTickRate(): Int {
        return super.getTickRate() * 3
    }

    companion object: MusicPredicateCompanion {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                "weather" to "Which weather the music should play for."
            )
    }

    enum class Weather {
        Clear,
        Rain,
        Thunder
    }
}