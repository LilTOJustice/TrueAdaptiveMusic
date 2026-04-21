package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.predicate.type.BasicPredicateType
import net.minecraft.client.Minecraft

class FlyingPredicate: BasicPredicateType("flying") {
    override fun validate(): Boolean {
        return Minecraft.getInstance().player?.isFallFlying ?: false
    }
}