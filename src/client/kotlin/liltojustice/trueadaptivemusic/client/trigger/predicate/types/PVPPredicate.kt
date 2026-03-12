package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity

class PVPPredicate(private val blockRadius: UInt): MusicPredicate() {
    override fun test(): Boolean {
        val client = MinecraftClient.getInstance()
        val world = client.world ?: return false
        val player = client.player ?: return false
        val teams = world.scoreboard.teams.filter { it.playerList.contains(player.name.string) }

        return world.entities.any { otherPlayer ->
            otherPlayer is PlayerEntity && teams.none { team ->
                team.playerList.contains(otherPlayer.name.string) &&
                        player.entityPos.distanceTo(otherPlayer.entityPos).toUInt() <= blockRadius
            }
        }
    }

    companion object: MusicPredicateCompanion {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                "blockRadius" to "Minimum radius for the enemy player to trigger the predicate."
            )

        override val displayName: String
            get() = "PVP"
    }
}