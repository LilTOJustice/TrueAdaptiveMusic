package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft
import net.minecraft.tags.FluidTags

class InLavaPredicate: MusicPredicate() {
    override fun getTickRate(): Int {
        return 2
    }

    override fun test(): Boolean {
        return Minecraft.getInstance().player?.isEyeInFluid(FluidTags.LAVA) ?: false
    }
}