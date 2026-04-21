package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.predicate.type.BasicPredicateType
import net.minecraft.client.Minecraft

class DayTimePredicate: BasicPredicateType("day") {
    override val tickRate: Int
        get() = super.tickRate * 2

    override fun validate(): Boolean {
        val level = Minecraft.getInstance().level ?: return false
        val time = level.overworldClockTime % 24000

        return time in 0..12999
    }
}