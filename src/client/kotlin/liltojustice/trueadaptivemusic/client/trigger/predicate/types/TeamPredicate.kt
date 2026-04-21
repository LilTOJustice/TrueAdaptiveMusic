package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.Minecraft

class TeamPredicate: StaticPredicateType<TeamPredicate.Arguments>("team") {
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(Arguments::teamId.name to "Id of the team to check if the player is on.")

    data class Arguments(val teamId: String): TriggerArguments()

    override fun validate(arguments: Arguments): Boolean {
        val minecraft = Minecraft.getInstance()
        val scoreboard = minecraft.level?.scoreboard ?: return false
        val playerName = minecraft.player?.name?.string ?: return false

        return scoreboard.playerTeams.firstOrNull { it.name == arguments.teamId }?.players?.contains(playerName)
            ?: false
    }
}