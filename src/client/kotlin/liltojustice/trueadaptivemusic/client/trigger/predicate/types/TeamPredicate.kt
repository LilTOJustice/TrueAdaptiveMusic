package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.MinecraftClient
import kotlin.reflect.typeOf

object TeamPredicate: StaticPredicateType<TeamPredicate.Arguments>(
    "team", typeOf<Arguments>()
) {
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(Arguments::teamId.name to "Id of the team to check if the player is on.")

    data class Arguments(val teamId: String): TriggerArguments()

    override fun test(arguments: Arguments): Boolean {
        val minecraft = MinecraftClient.getInstance()
        val scoreboard = minecraft.world?.scoreboard ?: return false
        val playerName = minecraft.player?.nameForScoreboard ?: return false

        return scoreboard.teams.firstOrNull { it.name == arguments.teamId }?.playerList?.contains(playerName) ?: false
    }
}