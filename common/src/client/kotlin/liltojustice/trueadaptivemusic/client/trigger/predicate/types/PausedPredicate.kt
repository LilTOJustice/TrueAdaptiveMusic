package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.predicate.type.BasicPredicateType
import net.minecraft.client.Minecraft

object PausedPredicate: BasicPredicateType("paused") {
    override fun test(): Boolean {
        val client = Minecraft.getInstance()
        return client.level != null && client.screen?.isPauseScreen ?: false
    }
}