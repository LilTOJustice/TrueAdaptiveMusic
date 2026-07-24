package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.predicate.type.BasicPredicateType
import net.minecraft.client.Minecraft

object PillagerRaidPredicate: BasicPredicateType("pillager_raid") {
    override fun test(): Boolean {
        val client = Minecraft.getInstance()
        val clientWorld = client.level ?: return false
        val serverWorld = client.singleplayerServer?.allLevels?.firstOrNull { world -> world.dimension() == clientWorld.dimension() }
            ?: return false

        return serverWorld.isRaided(client.player?.blockPosition() ?: return false)
    }
}