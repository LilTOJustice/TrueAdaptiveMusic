package liltojustice.trueadaptivemusic.client.predicate

import com.google.gson.JsonObject
import net.minecraft.client.MinecraftClient

class NightTimePredicate(partialPath: String)
    : MusicPredicate(partialPath) {
    override fun test(client: MinecraftClient): Boolean {
        val world = client.world ?: return false
        val time = world.timeOfDay % 24000

        return time in 13000..23999
    }

    override fun getIDs(): List<String> { return emptyList() }  // return immutable list, won't be using this

    companion object: MusicPredicateCompanion<NightTimePredicate>
    {
        override fun getTypeName(): String { return "night" }

        override fun fromJson(json: JsonObject, partialPath: String): NightTimePredicate
        {
            return NightTimePredicate(partialPath)
        }
    }
}