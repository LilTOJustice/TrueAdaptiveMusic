package liltojustice.trueadaptivemusic.client.trigger.predicate

import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.trigger.MusicTrigger
import net.minecraft.client.MinecraftClient

abstract class MusicPredicate: MusicTrigger() {
    private var lastResult = false

    protected abstract fun test(client: MinecraftClient): Boolean
    
    fun testPredicate(client: MinecraftClient): Boolean {
        val desiredTickRate = getTickRate()
        val actualTickRate = if (desiredTickRate < 1) 0 else desiredTickRate
        val tick = client.server?.ticks ?: 0
        if (tick % actualTickRate == 0) {
            lastResult = test(client)
        }

        return lastResult
    }

    final override fun getTypeName(): String {
        return if (this is ErrorPredicate)
            ErrorPredicate.NAME
        else
            TAMClient.predicateRegistry[this::class]
    }

    fun getTickRate(): Int {
        return 2
    }

    companion object: MusicPredicateCompanion<MusicPredicate> {
    }

    interface MusicPredicateCompanion<TSelf>: MusicTriggerCompanion<MusicPredicate>
            where TSelf: MusicPredicate {
    }
}