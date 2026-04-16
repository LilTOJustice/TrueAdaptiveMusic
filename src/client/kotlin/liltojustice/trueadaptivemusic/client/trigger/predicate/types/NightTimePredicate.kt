package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft

class NightTimePredicate: MusicPredicate() {
    override fun test(): Boolean {
        val level = Minecraft.getInstance().level ?: return false
        val time = level.overworldClockTime % 24000

        return time in 13000..23999
    }
}