package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft

class TeamPredicate(private val teamId: String): MusicPredicate() {
    override fun test(): Boolean {
        val minecraft = Minecraft.getInstance()
        val scoreboard = minecraft.level?.scoreboard ?: return false
        val playerName = minecraft.player?.name?.string ?: return false

        return scoreboard.playerTeams.firstOrNull { it.name == teamId }?.players?.contains(playerName) ?: false
    }

    companion object: MusicPredicateCompanion {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                TeamPredicate::teamId.name to "Id of the team to check if the player is on.")
    }
}