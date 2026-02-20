package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient

class InWaterPredicate: MusicPredicate() {
    private var tickBuffer = 0

    override fun test(): Boolean {
        val client = MinecraftClient.getInstance()
        val result = client.player?.isTouchingWater ?: false
        if (result) {
            tickBuffer = GRACE_PERIOD_TICKS
        }
        else if (tickBuffer-- > 0) {
            return true
        }

        return result
    }

    companion object: MusicPredicateCompanion<InWaterPredicate> {
        override fun fromJson(json: JsonObject): InWaterPredicate {
            return InWaterPredicate()
        }

        private const val GRACE_PERIOD_TICKS = 6
    }
}