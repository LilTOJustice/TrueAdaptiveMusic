package liltojustice.trueadaptivemusic.client.predicate

import com.google.gson.JsonObject
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.TitleScreen

class TitleScreenPredicate(partialPath: String)
    : MusicPredicate(partialPath) {

    override fun test(client: MinecraftClient): Boolean {
        return client.currentScreen is TitleScreen
    }

    override fun getIDs(): List<String> { return emptyList() }  // return immutable list, won't be using this

    companion object: MusicPredicateCompanion<TitleScreenPredicate> {
        override fun getTypeName(): String { return "title_screen" }

        override fun fromJson(json: JsonObject, partialPath: String): TitleScreenPredicate {
            return TitleScreenPredicate(partialPath)
        }
    }
}