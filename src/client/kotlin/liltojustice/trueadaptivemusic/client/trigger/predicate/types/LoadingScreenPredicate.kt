package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.LevelLoadingScreen

class LoadingScreenPredicate(): MusicPredicate() {
    override fun test(client: MinecraftClient): Boolean {
        return client.currentScreen is LevelLoadingScreen
    }

    companion object: MusicPredicateCompanion<LoadingScreenPredicate> {
        override fun getTypeName(): String { return "loading_screen" }

        override fun fromJson(json: JsonObject): LoadingScreenPredicate {
            return LoadingScreenPredicate()
        }
    }
}