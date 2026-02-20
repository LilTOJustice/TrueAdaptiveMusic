package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient

class TitleScreenPredicate(): MusicPredicate() {
    override fun test(): Boolean {
        val client = MinecraftClient.getInstance()
        return client.world == null
    }

    companion object: MusicPredicateCompanion<TitleScreenPredicate> {
        override fun fromJson(json: JsonObject): TitleScreenPredicate {
            return TitleScreenPredicate()
        }
    }
}