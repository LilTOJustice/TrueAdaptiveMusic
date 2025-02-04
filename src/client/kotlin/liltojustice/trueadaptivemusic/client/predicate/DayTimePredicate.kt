package liltojustice.trueadaptivemusic.client.predicate

import com.google.gson.JsonObject
import net.minecraft.client.MinecraftClient

class DayTimePredicate(partialPath: String)
    : MusicPredicate(partialPath) {
    override fun test(client: MinecraftClient): Boolean {
        val world = client.world ?: return false
        val time = world.timeOfDay % 24000

        return time in 0..12999
    }

    override fun getIDs(): List<String> { return emptyList() }  // return immutable list, won't be using this

    companion object: MusicPredicateCompanion<DayTimePredicate> {
        override fun getTypeName(): String { return "day" }

        override fun fromJson(json: JsonObject, partialPath: String): DayTimePredicate {
            return DayTimePredicate(partialPath)
        }
    }
}