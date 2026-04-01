package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient

class TeamPredicate(private val teamId: String): MusicPredicate() {
    override fun test(): Boolean {
        val client = MinecraftClient.getInstance()
        val scoreboard = client.world?.scoreboard ?: return false
        val playerName = client.player?.name?.string ?: return false

        return scoreboard.teams.firstOrNull { it.name == teamId }?.playerList?.contains(playerName) ?: false
    }

    companion object: MusicPredicateCompanion {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                TeamPredicate::teamId.name to "Id of the team to check if the player is on.")
    }
}