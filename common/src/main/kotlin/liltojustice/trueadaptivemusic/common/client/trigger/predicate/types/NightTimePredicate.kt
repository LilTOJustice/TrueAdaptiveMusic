package liltojustice.trueadaptivemusic.common.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.predicate.type.BasicPredicateType
import net.minecraft.client.Minecraft

object NightTimePredicate: BasicPredicateType("night") {
    override fun test(): Boolean {
        val level = Minecraft.getInstance().level ?: return false
        val time = level.overworldClockTime % 24000

        return time in 13000..23999
    }
}