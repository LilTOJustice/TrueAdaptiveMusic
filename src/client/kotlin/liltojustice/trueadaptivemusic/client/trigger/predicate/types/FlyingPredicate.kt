package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.predicate.type.BasicPredicateType
import net.minecraft.client.MinecraftClient

object FlyingPredicate: BasicPredicateType("flying") {
    override fun test(): Boolean {
        return MinecraftClient.getInstance().player?.isGliding ?: false
    }
}