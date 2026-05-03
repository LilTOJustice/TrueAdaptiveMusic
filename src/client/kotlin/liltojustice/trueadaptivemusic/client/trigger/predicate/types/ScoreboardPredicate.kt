package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.Minecraft
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
        val minecraft = Minecraft.getInstance()
        val scoreboard = minecraft.level?.scoreboard ?: return false
        val player = minecraft.player ?: return false
        val matchingObjective = scoreboard.objectives.firstOrNull { objective ->
            objective.name == arguments.objectiveId
        } ?: return false

        val matchingEntries = scoreboard.listPlayerScores(matchingObjective).filter { entry ->
            entry.owner == player.scoreboardName || player.team in scoreboard.playerTeams
        }

        val value = arguments.value
        return matchingEntries.any { matchingEntry ->
            when (arguments.comparison) {
                Comparison.Equal -> matchingEntry.value == value
                Comparison.NotEqual -> matchingEntry.value == value
                Comparison.Greater -> matchingEntry.value > value
                Comparison.GreaterOrEqual -> matchingEntry.value >= value
                Comparison.Lesser -> matchingEntry.value < value
                Comparison.LesserOrEqual -> matchingEntry.value <= value
            }
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