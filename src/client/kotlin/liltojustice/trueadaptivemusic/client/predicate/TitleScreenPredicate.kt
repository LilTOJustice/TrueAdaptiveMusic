package liltojustice.trueadaptivemusic.client.predicate

import com.google.gson.JsonObject
import net.minecraft.client.MinecraftClient

class TitleScreenPredicate(): MusicPredicate() {

    override fun test(client: MinecraftClient): Boolean {
        return client.world == null
    }

    override fun getPredicateParams(): List<PredicateParam> {
        return emptyList()
    }

    companion object: MusicPredicateCompanion<TitleScreenPredicate> {
        override fun getTypeName(): String { return "title_screen" }

        override fun fromJson(json: JsonObject): TitleScreenPredicate {
            return TitleScreenPredicate()
        }
    }
}