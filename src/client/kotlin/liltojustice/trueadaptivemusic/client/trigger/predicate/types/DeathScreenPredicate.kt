package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.DeathScreen

class DeathScreenPredicate: MusicPredicate() {
    override fun test(): Boolean {
        return Minecraft.getInstance().screen is DeathScreen
    }
}