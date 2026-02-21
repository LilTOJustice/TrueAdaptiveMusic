package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient

class PausedPredicate: MusicPredicate() {
    override fun test(): Boolean {
        val client = MinecraftClient.getInstance()
        return client.world != null && client.currentScreen?.shouldPause() ?: false
    }
}