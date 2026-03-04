package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient
import net.minecraft.world.GameMode

class GameModePredicate(private val gameMode: GameMode): MusicPredicate() {
    override fun test(): Boolean {
        val client = MinecraftClient.getInstance()
        val currentGameMode = client.networkHandler?.getPlayerListEntry(client.player?.uuid ?: return false)?.gameMode

        return currentGameMode == gameMode
    }

    companion object: MusicPredicateCompanion {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                "gameMode" to "Which game mode to be in for the music to play."
            )
    }
}