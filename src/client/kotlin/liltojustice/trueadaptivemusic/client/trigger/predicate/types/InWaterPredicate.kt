package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.predicate.type.BasicPredicateType
import net.minecraft.client.Minecraft
import net.minecraft.tags.FluidTags

class InWaterPredicate: BasicPredicateType("in_water") {
    override val tickRate: Int
        get() = 2

    override fun validate(): Boolean {
        return Minecraft.getInstance().player?.isEyeInFluid(FluidTags.WATER) ?: false
    }
}