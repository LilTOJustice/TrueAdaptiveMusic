package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft

class PausedPredicate: MusicPredicate() {
    override fun test(): Boolean {
        val minecraft = Minecraft.getInstance()

        return minecraft.level != null && minecraft.screen?.isPauseScreen ?: false
    }
}