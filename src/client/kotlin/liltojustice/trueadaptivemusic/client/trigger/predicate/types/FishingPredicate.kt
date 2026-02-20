package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient

class FishingPredicate: MusicPredicate() {
    override fun test(): Boolean {
        val client = MinecraftClient.getInstance()
        return client.player?.fishHook != null
    }

    companion object: MusicPredicateCompanion<FishingPredicate> {
        override fun fromJson(json: JsonObject): FishingPredicate {
            return FishingPredicate()
        }
    }
}