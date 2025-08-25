package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient

class RootPredicate(): MusicPredicate() {
    override fun test(client: MinecraftClient): Boolean { return true }

    override fun getTickRate(): Int {
        return 1
    }

    companion object: MusicPredicateCompanion<RootPredicate> {
        override fun fromJson(json: JsonObject): RootPredicate { return RootPredicate() }
    }
}
