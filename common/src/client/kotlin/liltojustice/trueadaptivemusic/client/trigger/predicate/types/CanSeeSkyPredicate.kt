package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.Minecraft
import kotlin.reflect.typeOf

object CanSeeSkyPredicate
    : StaticPredicateType<CanSeeSkyPredicate.Arguments>("can_see_sky", typeOf<Arguments>()) {
    override fun test(arguments: Arguments): Boolean {
        val minecraft = Minecraft.getInstance()
        val player = minecraft.player ?: return false
        val level = player.level()

        return if (arguments.allowWater) {
            level.canSeeSkyFromBelowWater(player.blockPosition())
        }
        else {
            level.canSeeSky(player.blockPosition())
        }
    }

    data class Arguments(val allowWater: Boolean): TriggerArguments()
}