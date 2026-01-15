package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.DeathScreen

class DeathScreenPredicate(): MusicPredicate() {
    override fun test(client: MinecraftClient): Boolean {
        return client.currentScreen is DeathScreen
    }

    companion object: MusicPredicateCompanion<DeathScreenPredicate> {
        override fun fromJson(json: JsonObject): DeathScreenPredicate {
            return DeathScreenPredicate()
        }
    }
}