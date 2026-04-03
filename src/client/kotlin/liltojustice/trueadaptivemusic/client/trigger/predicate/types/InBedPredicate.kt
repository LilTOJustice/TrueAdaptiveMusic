package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft

class InBedPredicate: MusicPredicate() {
    override fun test(): Boolean {
        return Minecraft.getInstance().player?.isSleeping ?: false
    }
}