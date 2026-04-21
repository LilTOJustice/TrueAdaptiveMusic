package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.predicate.type.BasicPredicateType
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.DeathScreen

class DeathScreenPredicate: BasicPredicateType("death_screen") {
    override fun validate(): Boolean {
        return Minecraft.getInstance().screen is DeathScreen
    }
}