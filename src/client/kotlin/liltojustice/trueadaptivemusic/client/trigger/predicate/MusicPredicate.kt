package liltojustice.trueadaptivemusic.client.trigger.predicate

import com.google.gson.Gson
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.trigger.MusicTrigger
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import net.minecraft.client.MinecraftClient

abstract class MusicPredicate: MusicTrigger<MusicPredicate.Parameters>() {
    init {
        parameters = Parameters.default()
    }

    private var lastResult = false
    private var ticksSinceResult = getFixedTickRate()

    protected abstract fun test(client: MinecraftClient): Boolean

    final override fun getTypeName(): String {
        return if (this is ErrorPredicate)
            ErrorPredicate.NAME
        else
            TAMClient.predicateRegistry[this::class]
    }

    final override fun initParams(json: JsonObject) {
        parameters = json.get("parameters")
            ?.let { Gson().fromJson<Parameters>(it, Parameters::class.java) } ?: Parameters()
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

    data class Parameters(var trackDelay: UInt = 0U, var trackDelayNoise: UInt = 0U): MusicTrigger.Parameters() {
        companion object: ParametersCompanion<MusicEvent.Parameters> {
            override fun default(): Parameters {
                return Parameters()
            }
        }
    }

    interface MusicPredicateCompanion<TSelf>: MusicTriggerCompanion<MusicPredicate>
            where TSelf: MusicPredicate {
    }
}