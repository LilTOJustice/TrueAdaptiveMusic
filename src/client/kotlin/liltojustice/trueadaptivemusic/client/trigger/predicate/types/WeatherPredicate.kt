package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient
import net.minecraft.util.JsonHelper

class WeatherPredicate(private val weather: Weather): MusicPredicate() {
    override fun test(client: MinecraftClient): Boolean {
        val properties = client.world?.levelProperties ?: return false

        return when(weather) {
            Weather.Clear -> !properties.isRaining
            Weather.Rain -> properties.isRaining
            Weather.Thunder -> properties.isThundering
        }
    }

    override fun toJson(): JsonObject {
        val result = super.toJson()
        result.addProperty(FIELD_NAME, weather.name)

        return result
    }

    companion object: MusicPredicateCompanion<WeatherPredicate> {
        override fun getTypeName(): String { return "weather" }

        override fun fromJson(json: JsonObject): WeatherPredicate {
            return WeatherPredicate(Weather.valueOf(JsonHelper.getString(json, FIELD_NAME)))
        }

        private const val FIELD_NAME = "weatherType"
    }

    enum class Weather {
        Clear,
        Rain,
        Thunder
    }
}