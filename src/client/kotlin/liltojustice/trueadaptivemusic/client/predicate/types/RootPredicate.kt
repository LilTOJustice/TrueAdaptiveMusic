package liltojustice.trueadaptivemusic.client.predicate.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient

class RootPredicate(): MusicPredicate() {
    override fun test(client: MinecraftClient): Boolean { return true }

    companion object: MusicPredicateCompanion<RootPredicate> {
        override fun getTypeName(): String { return "root" }

        override fun fromJson(json: JsonObject): RootPredicate { return RootPredicate() }
    }
}
