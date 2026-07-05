package liltojustice.trueadaptivemusic.common.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.predicate.type.BasicPredicateType
import net.minecraft.client.Minecraft

object TitleScreenPredicate: BasicPredicateType("title_screen") {
    override fun test(): Boolean {
        return Minecraft.getInstance().level == null
    }
}