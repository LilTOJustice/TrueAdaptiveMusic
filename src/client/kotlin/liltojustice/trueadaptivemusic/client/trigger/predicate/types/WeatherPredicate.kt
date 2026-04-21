package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.Minecraft

class WeatherPredicate: StaticPredicateType<WeatherPredicate.Arguments>("weather") {
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(Arguments::weather.name to "Which weather the music should play for.")
    override val tickRate: Int
        get() = super.tickRate * 3
    data class Arguments(val weather: Weather): TriggerArguments()

    override fun validate(arguments: Arguments): Boolean {
        val minecraft = Minecraft.getInstance()
        val level = minecraft.level ?: return false

        return when(arguments.weather) {
            Weather.Clear -> !level.isRaining
            Weather.Rain -> level.isRaining
            Weather.Thunder -> level.isThundering
        }
    }

    enum class Weather {
        Clear,
        Rain,
        Thunder
    }
}