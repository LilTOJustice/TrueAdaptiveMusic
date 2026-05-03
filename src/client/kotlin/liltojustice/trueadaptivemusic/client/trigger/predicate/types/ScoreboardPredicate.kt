package liltojustice.trueadaptivemusic.client.trigger.predicate.types

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
        val minecraft = MinecraftClient.getInstance()
        val scoreboard = minecraft.world?.scoreboard ?: return false
        val playerName = minecraft.player?.name?.string ?: return false
        val matchingObjective = scoreboard.objectives.firstOrNull { objective ->
            objective.name == arguments.objectiveId
        } ?: return false
        val matchingEntry = scoreboard.getPlayerScore(playerName, matchingObjective)

        val value = arguments.value
        return when (arguments.comparison) {
            Comparison.Equal -> matchingEntry.score == value
            Comparison.NotEqual -> matchingEntry.score == value
            Comparison.Greater -> matchingEntry.score > value
            Comparison.GreaterOrEqual -> matchingEntry.score >= value
            Comparison.Lesser -> matchingEntry.score < value
            Comparison.LesserOrEqual -> matchingEntry.score <= value
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