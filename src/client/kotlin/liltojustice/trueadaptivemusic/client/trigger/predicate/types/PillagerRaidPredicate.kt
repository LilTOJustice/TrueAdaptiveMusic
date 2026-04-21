package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.predicate.type.BasicPredicateType
import net.minecraft.client.Minecraft

class PillagerRaidPredicate: BasicPredicateType("pillager_raid") {
    override fun validate(): Boolean {
        val client = Minecraft.getInstance()
        val clientLevel = client.level ?: return false
        val serverWorld = client.singleplayerServer?.allLevels?.firstOrNull { level ->
            level.dimension().identifier() == clientLevel.dimension().identifier() }
            ?: return false

        return serverWorld.isRaided(client.player?.blockPosition() ?: return false)
    }
}