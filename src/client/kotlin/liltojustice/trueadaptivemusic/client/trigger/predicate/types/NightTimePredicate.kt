package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient

class NightTimePredicate: MusicPredicate() {
    override fun test(): Boolean {
        val client = MinecraftClient.getInstance()
        val world = client.world ?: return false
        val time = world.timeOfDay % 24000

        return time in 13000..23999
    }
}