package liltojustice.trueadaptivemusic.common.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.predicate.type.BasicPredicateType
import net.minecraft.client.Minecraft

object PausedPredicate: BasicPredicateType("paused") {
    override fun test(): Boolean {
        val minecraft = Minecraft.getInstance()

        return minecraft.level != null && minecraft.gui.screen()?.isPauseScreen ?: false
    }
}