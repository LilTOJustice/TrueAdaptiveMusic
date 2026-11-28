package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.predicate.type.BasicPredicateType
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.contents.TranslatableContents

object PillagerRaidPredicate: BasicPredicateType("pillager_raid") {
    override fun test(): Boolean {
        return Minecraft.getInstance().gui.hud.bossOverlay.events.any {
            (it.value.name.contents as? TranslatableContents)?.key == "event.minecraft.raid"
        }
    }
}