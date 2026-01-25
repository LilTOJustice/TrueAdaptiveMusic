package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient

class InFluidPredicate: MusicPredicate() {
    private var tickBuffer = 0

    override fun test(client: MinecraftClient): Boolean {
        val result = client.player?.isInFluid ?: false
        if (result) {
            tickBuffer = GRACE_PERIOD_TICKS
        }
        else if (tickBuffer-- >= 0) {
            return true
        }

        return result
    }

    companion object: MusicPredicateCompanion<InFluidPredicate> {
        override fun fromJson(json: JsonObject): InFluidPredicate {
            return InFluidPredicate()
        }

        private const val GRACE_PERIOD_TICKS = 5
    }
}