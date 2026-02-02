package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient

class InBedPredicate: MusicPredicate() {
    override fun test(client: MinecraftClient): Boolean {
        return client.player?.isSleeping ?: false
    }

    companion object: MusicPredicateCompanion<InBedPredicate> {
        override fun fromJson(json: JsonObject): InBedPredicate {
            return InBedPredicate()
        }
    }
}