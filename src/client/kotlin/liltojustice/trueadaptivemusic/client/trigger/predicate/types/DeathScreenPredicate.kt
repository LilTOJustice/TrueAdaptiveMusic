package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.predicate.type.BasicPredicateType
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.DeathScreen

object DeathScreenPredicate: BasicPredicateType("death_screen") {
    override fun test(): Boolean {
        val client = MinecraftClient.getInstance()
        return client.currentScreen is DeathScreen
    }
}