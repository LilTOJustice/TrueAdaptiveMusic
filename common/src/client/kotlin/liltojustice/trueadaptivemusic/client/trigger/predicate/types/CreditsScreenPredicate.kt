package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.predicate.type.BasicPredicateType
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.WinScreen

object CreditsScreenPredicate: BasicPredicateType("credits_screen") {
    override fun test(): Boolean {
        return Minecraft.getInstance().gui.screen() is WinScreen
    }
}