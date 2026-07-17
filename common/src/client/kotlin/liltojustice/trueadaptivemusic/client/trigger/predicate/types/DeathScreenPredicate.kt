package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.predicate.type.BasicPredicateType
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.DeathScreen

object DeathScreenPredicate: BasicPredicateType("death_screen") {
    override fun test(): Boolean {
        val client = Minecraft.getInstance()
        return client.screen is DeathScreen
    }
}