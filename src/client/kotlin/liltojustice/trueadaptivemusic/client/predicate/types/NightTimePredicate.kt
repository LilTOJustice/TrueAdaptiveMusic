package liltojustice.trueadaptivemusic.client.predicate.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient

class NightTimePredicate: MusicPredicate() {
    override fun test(client: MinecraftClient): Boolean {
        val world = client.world ?: return false
        val time = world.timeOfDay % 24000

        return time in 13000..23999
    }

    companion object: MusicPredicateCompanion<NightTimePredicate>
    {
        override fun getTypeName(): String { return "night" }

        override fun fromJson(json: JsonObject): NightTimePredicate
        {
            return NightTimePredicate()
        }
    }
}