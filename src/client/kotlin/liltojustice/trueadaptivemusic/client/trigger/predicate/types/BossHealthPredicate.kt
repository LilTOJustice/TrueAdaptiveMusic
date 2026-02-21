package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient

class BossHealthPredicate(private val direction: Direction, private val healthPercentage: Int): MusicPredicate() {
    override fun test(): Boolean {
        val client = MinecraftClient.getInstance()
        return client.inGameHud.bossBarHud.bossBars.any { bossBar ->
            healthTest((healthPercentage / 100F), direction, bossBar.value.percent) }
    }

    override fun getTickRate(): Int {
        return super.getTickRate() * 4
    }

    companion object: MusicPredicateCompanion {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                "direction" to "Whether the music should play above or below the given health percentage.",
                "healthPercentage" to "The threshold at which the predicate switches."
            )

        private fun healthTest(thresholdPercentage: Float, direction: Direction, currentPercentage: Float): Boolean {
            return when (direction) {
                Direction.Greater -> currentPercentage > thresholdPercentage
                Direction.Lesser -> currentPercentage < thresholdPercentage
            }
        }
    }

    enum class Direction {
        Greater,
        Lesser
    }
}