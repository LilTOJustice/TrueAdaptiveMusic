package liltojustice.trueadaptivemusic.client.predicate

import com.google.gson.JsonObject
import net.minecraft.client.MinecraftClient

class DayTimePredicate: MusicPredicate() {
    override fun test(client: MinecraftClient): Boolean {
        val world = client.world ?: return false
        val time = world.timeOfDay % 24000

        return time in 0..12999
    }

    override fun getPredicateParams(): List<PredicateParam> {
        return emptyList()
    }

    companion object: MusicPredicateCompanion<DayTimePredicate> {
        override fun getTypeName(): String { return "day" }

        override fun fromJson(json: JsonObject): DayTimePredicate {
            return DayTimePredicate()
        }
    }
}