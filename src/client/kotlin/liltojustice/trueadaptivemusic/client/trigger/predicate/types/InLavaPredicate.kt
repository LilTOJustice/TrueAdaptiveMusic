package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient
import net.minecraft.registry.tag.FluidTags

class InLavaPredicate: MusicPredicate() {
    override fun getTickRate(): Int {
        return 2
    }

    override fun test(): Boolean {
        val client = MinecraftClient.getInstance()
        return client.player?.isSubmergedIn(FluidTags.LAVA) ?: false
    }
}