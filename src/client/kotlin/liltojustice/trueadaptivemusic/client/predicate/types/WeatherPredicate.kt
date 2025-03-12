package liltojustice.trueadaptivemusic.client.predicate.types

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import liltojustice.trueadaptivemusic.client.enum.Weather
import net.minecraft.client.MinecraftClient
import net.minecraft.util.JsonHelper

class WeatherPredicate(private val weather: Weather): MusicPredicate() {
    override fun test(client: MinecraftClient): Boolean {
        val properties = client.world?.levelProperties ?: return false

        return when(weather) {
            Weather.Clear ->
                !properties.isRaining
            Weather.Rain ->
                properties.isRaining
            Weather.Thunder ->
                properties.isThundering
        }
    }

    override fun toJson(): JsonObject {
        val result = super.toJson()
        result.add(FIELD_NAME, JsonPrimitive(weather.name))

        return result
    }

    companion object: MusicPredicateCompanion<WeatherPredicate> {
        override fun getTypeName(): String { return "weather" }

        override fun fromJson(json: JsonObject): WeatherPredicate {
            return WeatherPredicate(Weather.valueOf(JsonHelper.getString(json, FIELD_NAME)))
        }

        private const val FIELD_NAME = "weatherType"
    }
}