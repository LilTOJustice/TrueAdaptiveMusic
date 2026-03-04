package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.DeathScreen

class DeathScreenPredicate: MusicPredicate() {
    override fun test(): Boolean {
        val client = MinecraftClient.getInstance()
        return client.currentScreen is DeathScreen
    }
}