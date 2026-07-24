package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.predicate.type.BasicPredicateType
import net.minecraft.client.Minecraft

object DayTimePredicate: BasicPredicateType("day") {
    override val tickRate: Int
        get() = super.tickRate * 2

    override fun test(): Boolean {
        val level = Minecraft.getInstance().level ?: return false
        val time = level.dayTime % 24000

        return time in 0..12999
    }
}