package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient
import net.minecraft.util.JsonHelper

class BossHealthPredicate(private val direction: Direction, private val healthPercentage: Int): MusicPredicate() {
    override fun test(client: MinecraftClient): Boolean {
        return client.inGameHud.bossBarHud.bossBars.any { bossBar ->
            healthTest((healthPercentage / 100F), direction, bossBar.value.percent) }
    }

    override fun getTickRate(): Int {
        return super.getTickRate() * 4
    }

    override fun toJson(): JsonObject {
        val result = JsonObject()
        result.addProperty("direction", direction.name)
        result.addProperty("healthPercentage", healthPercentage)

        return result
    }

    companion object: MusicPredicateCompanion<BossHealthPredicate> {
        override val descriptions: Map<String, String>
            get() = super.descriptions + mapOf(
                "direction" to "Whether the music should play above or below the given health percentage.",
                "healthPercentage" to "The threshold at which the predicate switches."
            )

        override fun fromJson(json: JsonObject): BossHealthPredicate {
            return BossHealthPredicate(
                Direction.valueOf(JsonHelper.getString(json, "direction")),
                JsonHelper.getInt(json, "healthPercentage"))
        }

        private fun healthTest(thresholdPercentage: Float, direction: Direction, currentPercentage: Float): Boolean {
            return when (direction) {
                Direction.Greater -> currentPercentage > thresholdPercentage
                Direction.Lesser -> currentPercentage < thresholdPercentage
            }
        }
    }

    enum class Direction {
        Greater,
        Lesser
    }
}