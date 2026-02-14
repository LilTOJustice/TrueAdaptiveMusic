package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient
import net.minecraft.util.JsonHelper

class HungerPredicate(private val direction: Direction, private val hungerPercentage: Int): MusicPredicate() {
    override fun test(client: MinecraftClient): Boolean {
        if (client.player?.isCreative == true || client.player?.isSpectator == true) {
            return false
        }

        val currentPercentage = (client.player?.hungerManager?.foodLevel ?: return false) / 20F
        val thresholdPercentage = hungerPercentage / 100F

        return when (direction) {
            Direction.Greater -> currentPercentage > thresholdPercentage
            Direction.Lesser -> currentPercentage < thresholdPercentage
        }
    }

    override fun getTickRate(): Int {
        return super.getTickRate() * 4
    }

    override fun toJson(): JsonObject {
        val result = JsonObject()
        result.addProperty("direction", direction.name)
        result.addProperty("hungerPercentage", hungerPercentage)

        return result
    }

    companion object: MusicPredicateCompanion<HungerPredicate> {
        override val descriptions: Map<String, String>
            get() = super.descriptions + mapOf(
                "direction" to "Whether the music should play when the player's hunger percentage is above or " +
                        "below the given percentage.",
                "hungerPercentage" to "Threshold at which the predicate should switch."
            )

        override fun fromJson(json: JsonObject): HungerPredicate {
            return HungerPredicate(
                Direction.valueOf(JsonHelper.getString(json, "direction")),
                JsonHelper.getInt(json, "hungerPercentage"))
        }
    }

    enum class Direction {
        Greater,
        Lesser
    }
}