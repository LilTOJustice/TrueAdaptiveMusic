package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.predicate.type.BasicPredicateType
import net.minecraft.client.Minecraft

object InBedPredicate: BasicPredicateType("in_bed") {
    override fun test(): Boolean {
        val client = Minecraft.getInstance()
        return client.player?.isSleeping ?: false
    }
}