package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.CreditsScreen

class CreditsScreenPredicate: MusicPredicate() {
    override fun test(client: MinecraftClient): Boolean {
        return client.currentScreen is CreditsScreen
    }

    companion object: MusicPredicateCompanion<CreditsScreenPredicate> {
        override fun fromJson(json: JsonObject): CreditsScreenPredicate {
            return CreditsScreenPredicate()
        }
    }
}