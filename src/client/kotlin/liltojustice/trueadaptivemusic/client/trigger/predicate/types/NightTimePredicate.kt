package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.predicate.type.BasicPredicateType
import net.minecraft.client.MinecraftClient

object NightTimePredicate: BasicPredicateType("night") {
    override fun test(): Boolean {
        val level = MinecraftClient.getInstance().world ?: return false
        val time = level.timeOfDay % 24000

        return time in 13000..23999
    }
}