package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.predicate.type.BasicPredicateType
import net.minecraft.client.MinecraftClient

object TitleScreenPredicate: BasicPredicateType("title_screen") {
    override fun test(): Boolean {
        val client = MinecraftClient.getInstance()
        return client.world == null
    }
}