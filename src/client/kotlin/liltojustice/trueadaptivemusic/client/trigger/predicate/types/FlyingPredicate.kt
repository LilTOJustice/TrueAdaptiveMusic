package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient

class FlyingPredicate: MusicPredicate() {
    override fun test(client: MinecraftClient): Boolean {
        return client.player?.isFallFlying ?: false
    }

    companion object: MusicPredicateCompanion<FlyingPredicate> {
        override fun fromJson(json: JsonObject): FlyingPredicate {
            return FlyingPredicate()
        }
    }
}