package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.predicate.type.BasicPredicateType
import net.minecraft.client.MinecraftClient

object FishingPredicate: BasicPredicateType("fishing") {
    override fun test(): Boolean {
        return MinecraftClient.getInstance().player?.fishHook != null
    }
}