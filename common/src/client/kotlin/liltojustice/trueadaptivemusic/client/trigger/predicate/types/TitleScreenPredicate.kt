package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.predicate.type.BasicPredicateType
import net.minecraft.client.Minecraft

object TitleScreenPredicate: BasicPredicateType("title_screen") {
    override fun test(): Boolean {
        val client = Minecraft.getInstance()
        return client.level == null
    }
}