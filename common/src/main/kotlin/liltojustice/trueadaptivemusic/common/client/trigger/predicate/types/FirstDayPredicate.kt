package liltojustice.trueadaptivemusic.common.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.predicate.type.BasicPredicateType
import net.minecraft.client.Minecraft

object FirstDayPredicate: BasicPredicateType("first_day") {
    override fun test(): Boolean {
        val time = Minecraft.getInstance().level?.overworldClockTime ?: return false

        return time <= 24000L
    }
}