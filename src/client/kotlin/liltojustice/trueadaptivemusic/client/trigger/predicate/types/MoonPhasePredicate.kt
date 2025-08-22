package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient
import net.minecraft.util.JsonHelper
import kotlin.ranges.contains

class MoonPhasePredicate(private val moonPhase: MoonPhase): MusicPredicate() {
    override fun test(client: MinecraftClient): Boolean {
        val world = client.world ?: return false
        val currentPhase = world.moonPhase
        val time = world.timeOfDay % 24000

        return time in 13000..23999 && when(moonPhase) {
            MoonPhase.Full -> currentPhase == 0
            MoonPhase.New -> currentPhase == 4
        }
    }

    override fun toJson(): JsonObject {
        val result = JsonObject()
        result.addProperty(FIELD_NAME, moonPhase.name)

        return result
    }

    companion object: MusicPredicateCompanion<MoonPhasePredicate> {
        override fun fromJson(json: JsonObject): MoonPhasePredicate {
            return MoonPhasePredicate(MoonPhase.valueOf(JsonHelper.getString(json, FIELD_NAME)))
        }

        private const val FIELD_NAME = "moonPhase"
    }

    enum class MoonPhase {
        New,
        Full
    }
}