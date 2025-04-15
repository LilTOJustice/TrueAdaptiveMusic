package liltojustice.trueadaptivemusic.client.predicate.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient

class TitleScreenPredicate(): MusicPredicate() {
    override fun test(client: MinecraftClient): Boolean {
        return client.world == null
    }

    companion object: MusicPredicateCompanion<TitleScreenPredicate> {
        override fun getTypeName(): String { return "title_screen" }

        override fun fromJson(json: JsonObject): TitleScreenPredicate {
            return TitleScreenPredicate()
        }
    }
}