package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.WinScreen

class CreditsScreenPredicate: MusicPredicate() {
    override fun test(): Boolean {
        return Minecraft.getInstance().screen is WinScreen
    }
}