package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.predicate.type.BasicPredicateType
import net.minecraft.client.Minecraft

class FirstDayPredicate: BasicPredicateType("first_day") {
    override fun validate(): Boolean {
        val time = Minecraft.getInstance().level?.overworldClockTime ?: return false

        return time <= 24000L
    }
}