package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.predicate.type.BasicPredicateType
import net.minecraft.client.MinecraftClient

object FirstDayPredicate: BasicPredicateType("first_day") {
    override fun test(): Boolean {
        val time = MinecraftClient.getInstance().world?.time ?: return false

        return time <= 24000L
    }
}