package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient

class DayTimePredicate: MusicPredicate() {
    override fun test(): Boolean {
        val level = MinecraftClient.getInstance().world ?: return false
        val time = level.timeOfDay % 24000

        return time in 0..12999
    }

    override fun getTickRate(): Int {
        return super.getTickRate() * 2
    }
}