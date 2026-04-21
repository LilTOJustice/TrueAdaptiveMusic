package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.predicate.type.BasicPredicateType
import net.minecraft.client.Minecraft

class FishingPredicate: BasicPredicateType("fishing") {
    override fun validate(): Boolean {
        return Minecraft.getInstance().player?.fishing != null
    }
}