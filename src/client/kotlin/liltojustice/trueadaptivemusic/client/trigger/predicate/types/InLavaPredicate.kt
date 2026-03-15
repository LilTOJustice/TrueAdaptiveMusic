package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient

class InLavaPredicate: MusicPredicate() {
    override fun getTickRate(): Int {
        return 10
    }

    override fun test(): Boolean {
        val client = MinecraftClient.getInstance()
        return client.player?.isInLava ?: false
    }
}