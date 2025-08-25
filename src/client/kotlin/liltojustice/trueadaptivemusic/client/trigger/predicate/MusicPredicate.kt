package liltojustice.trueadaptivemusic.client.trigger.predicate

import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.trigger.MusicTrigger
import net.minecraft.client.MinecraftClient

abstract class MusicPredicate: MusicTrigger() {
    private var lastResult = false
    private var ticksSinceResult = getFixedTickRate()

    protected abstract fun test(client: MinecraftClient): Boolean

    final override fun getTypeName(): String {
        return if (this is ErrorPredicate)
            ErrorPredicate.NAME
        else
            TAMClient.predicateRegistry[this::class]
    }

    fun testPredicate(client: MinecraftClient): Boolean {
        val tickRate = getFixedTickRate()
        if (ticksSinceResult++ == tickRate) {
            ticksSinceResult = 1

            lastResult = test(client)
            return lastResult
        }

        return lastResult
    }

    open fun getTickRate(): Int {
        return 2
    }

    private fun getFixedTickRate(): Int {
        val desiredTickRate = getTickRate()
        return if (desiredTickRate < 1) 0 else desiredTickRate
    }

    companion object: MusicPredicateCompanion<MusicPredicate> {
    }

    interface MusicPredicateCompanion<TSelf>: MusicTriggerCompanion<MusicPredicate>
            where TSelf: MusicPredicate {
    }
}