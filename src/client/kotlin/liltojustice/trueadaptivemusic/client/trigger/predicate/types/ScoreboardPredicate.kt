package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft

class ScoreboardPredicate(
    private val objectiveId: String,
    private val value: Int,
    private val comparison: Comparison
): MusicPredicate() {
    override fun test(): Boolean {
        val minecraft = Minecraft.getInstance()
        val scoreboard = minecraft.level?.scoreboard ?: return false
        val player = minecraft.player ?: return false
        val matchingObjective = scoreboard.objectives.firstOrNull { objective ->
            objective.name == objectiveId
        } ?: return false

        val matchingEntries = scoreboard.listPlayerScores(matchingObjective).filter { entry ->
            entry.owner == player.name.string || player.team in scoreboard.playerTeams
        }

        return matchingEntries.any { matchingEntry ->
            when (comparison) {
                Comparison.Equal -> matchingEntry.value == value
                Comparison.NotEqual -> matchingEntry.value == value
                Comparison.Greater -> matchingEntry.value > value
                Comparison.GreaterOrEqual -> matchingEntry.value >= value
                Comparison.Lesser -> matchingEntry.value < value
                Comparison.LesserOrEqual -> matchingEntry.value <= value
            }
        }
    }

    companion object: MusicPredicateCompanion {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                ScoreboardPredicate::objectiveId.name to "Id of the scoreboard objective to track.",
                ScoreboardPredicate::value.name to "Value to compare to the objective value.",
                ScoreboardPredicate::comparison.name to "How to compare the objective value to the given value."
            )
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