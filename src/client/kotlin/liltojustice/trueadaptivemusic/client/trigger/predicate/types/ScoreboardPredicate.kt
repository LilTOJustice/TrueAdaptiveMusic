package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.TAMNetworkingClient
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.MinecraftClient
import kotlin.reflect.typeOf

object ScoreboardPredicate: StaticPredicateType<ScoreboardPredicate.Arguments>(
    "scoreboard", typeOf<Arguments>()
) {
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::objectiveId.name to "Id of the scoreboard objective to track.",
            Arguments::value.name to "Value to compare to the objective value.",
            Arguments::comparison.name to "How to compare the objective value to the given value."
        )

    data class Arguments(val objectiveId: String, val value: Int, val comparison: Comparison): TriggerArguments()

    override fun test(arguments: Arguments): Boolean {
        TAMNetworkingClient.scoreboardState[arguments.objectiveId]?.let {
            return compare(arguments.comparison, it, arguments.value)
        }

        val minecraft = MinecraftClient.getInstance()
        val scoreboard = minecraft.world?.scoreboard ?: return false
        val player = minecraft.player?.nameForScoreboard ?: return false
        val matchingObjective = scoreboard.objectives.firstOrNull { objective ->
            objective.name == arguments.objectiveId
        } ?: return false

        val matchingEntries = scoreboard.getScoreboardEntries(matchingObjective).filter { entry ->
            entry.owner == player || scoreboard.teams.any { team -> team.playerList.any { it == player } }
        }

        return matchingEntries.any { matchingEntry ->
            compare(arguments.comparison, matchingEntry.value(), arguments.value)
        }
    }

    private fun compare(comparison: Comparison, value: Int, otherValue: Int): Boolean {
        return when (comparison) {
            Comparison.Equal -> value == otherValue
            Comparison.NotEqual -> value == otherValue
            Comparison.Greater -> value > otherValue
            Comparison.GreaterOrEqual -> value >= otherValue
            Comparison.Lesser -> value < otherValue
            Comparison.LesserOrEqual -> value <= otherValue
        }
    }

    enum class Comparison {
        Equal,
        NotEqual,
        Greater,
        GreaterOrEqual,
        Lesser,
        LesserOrEqual
    }
}