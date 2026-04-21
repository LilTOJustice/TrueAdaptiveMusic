package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.predicate.type.BasicPredicateType
import net.minecraft.client.Minecraft

class PausedPredicate: BasicPredicateType("paused") {
    override fun validate(): Boolean {
        val minecraft = Minecraft.getInstance()

        return minecraft.level != null && minecraft.screen?.isPauseScreen ?: false
    }
}