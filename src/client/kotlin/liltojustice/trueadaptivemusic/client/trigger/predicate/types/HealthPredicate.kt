package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient

class HealthPredicate(private val healthType: HealthType, private val direction: Direction, private val health: Int): MusicPredicate() {
    override fun test(): Boolean {
        val client = MinecraftClient.getInstance()
        val player = client.player ?: return false
        val typeAdjusted = if (healthType == HealthType.Percentage)
            player.maxHealth * (health / 100F)
        else
            health.toFloat()

        return when (direction) {
            Direction.Greater -> player.health > typeAdjusted
            Direction.GreaterOrEqual -> player.health >= typeAdjusted
            Direction.Lesser -> player.health < typeAdjusted
            Direction.LesserOrEqual -> player.health <= typeAdjusted
        }
    }

    companion object: MusicPredicateCompanion {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                "healthType" to "Whether the health setting is a value or percentage.",
                "direction" to "Whether the music should play above or below the health setting.",
                "health" to "Threshold at which the predicate should switch."
            )
    }

    enum class HealthType {
        Value,
        Percentage
    }

    enum class Direction {
        Greater,
        GreaterOrEqual,
        Lesser,
        LesserOrEqual
    }
}