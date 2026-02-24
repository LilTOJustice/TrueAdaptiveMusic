package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient

class PillagerRaidPredicate: MusicPredicate() {
    override fun test(): Boolean {
        val client = MinecraftClient.getInstance()
        val clientWorld = client.world ?: return false
        val serverWorld = client.server?.worlds?.firstOrNull { world -> world.registryKey == clientWorld.registryKey }
            ?: return false

        return serverWorld.hasRaidAt(client.player?.blockPos ?: return false)
    }
}