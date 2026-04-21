package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.predicate.type.BasicPredicateType
import net.minecraft.client.Minecraft

class InBedPredicate: BasicPredicateType("in_bed") {
    override fun validate(): Boolean {
        return Minecraft.getInstance().player?.isSleeping ?: false
    }
}