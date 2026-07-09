package liltojustice.trueadaptivemusic.common.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.predicate.type.BasicPredicateType
import net.minecraft.client.Minecraft

object FlyingPredicate: BasicPredicateType("flying") {
    override fun test(): Boolean {
        return Minecraft.getInstance().player?.isFallFlying ?: false
    }
}