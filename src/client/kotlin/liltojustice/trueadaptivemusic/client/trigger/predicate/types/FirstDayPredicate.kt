package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient

class FirstDayPredicate: MusicPredicate() {
    override fun test(): Boolean {
        val client = MinecraftClient.getInstance()
        val time = client.world?.time ?: return false

        return time <= 24000L
    }
}