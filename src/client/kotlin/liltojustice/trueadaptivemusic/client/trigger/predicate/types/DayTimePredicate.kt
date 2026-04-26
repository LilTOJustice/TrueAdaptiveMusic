package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.predicate.type.BasicPredicateType
import net.minecraft.client.MinecraftClient

object DayTimePredicate: BasicPredicateType("day") {
    override val tickRate: Int
        get() = super.tickRate * 2

    override fun test(): Boolean {
        val level = MinecraftClient.getInstance().world ?: return false
        val time = level.timeOfDay % 24000

        return time in 0..12999
    }
}