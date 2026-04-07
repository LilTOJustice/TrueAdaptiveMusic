package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft

class PillagerRaidPredicate: MusicPredicate() {
    override fun test(): Boolean {
        val client = Minecraft.getInstance()
        val clientLevel = client.level ?: return false
        val serverWorld = client.singleplayerServer?.allLevels?.firstOrNull { level ->
            level.dimension().identifier() == clientLevel.dimension().identifier() }
            ?: return false

        return serverWorld.isRaided(client.player?.blockPosition() ?: return false)
    }
}