package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.predicate.type.BasicPredicateType
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.WinScreen

class CreditsScreenPredicate: BasicPredicateType("credits_screen") {
    override fun validate(): Boolean {
        return Minecraft.getInstance().screen is WinScreen
    }
}