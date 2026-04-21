package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.predicate.type.BasicPredicateType
import net.minecraft.client.Minecraft

class TitleScreenPredicate: BasicPredicateType("title_screen") {
    override fun validate(): Boolean {
        return Minecraft.getInstance().level == null
    }
}