package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient

class DayTimePredicate: MusicPredicate() {
    override fun test(client: MinecraftClient): Boolean {
        val world = client.world ?: return false
        val time = world.timeOfDay % 24000

        return time in 0..12999
    }

    override fun getTickRate(): Int {
        return 100
    }

    companion object: MusicPredicateCompanion<DayTimePredicate> {
        override fun fromJson(json: JsonObject): DayTimePredicate {
            return DayTimePredicate()
        }
    }
}