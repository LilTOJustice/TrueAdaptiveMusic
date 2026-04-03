package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft

class FirstDayPredicate: MusicPredicate() {
    override fun test(): Boolean {
        val time = Minecraft.getInstance().level?.gameTime ?: return false

        return time <= 24000L
    }
}