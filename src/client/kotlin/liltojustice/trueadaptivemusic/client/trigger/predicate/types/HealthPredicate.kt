package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft

class HealthPredicate(private val healthType: HealthType, private val direction: Direction, private val health: Int): MusicPredicate() {
    override fun test(): Boolean {
        val minecraft = Minecraft.getInstance()
        val player = minecraft.player ?: return false
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
                HealthPredicate::healthType.name to "Whether the health setting is a value or percentage.",
                HealthPredicate::direction.name to "Whether the music should play above or below the health setting.",
                HealthPredicate::health.name to "Threshold at which the predicate should switch."
            )
    }

    @Suppress("unused")
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